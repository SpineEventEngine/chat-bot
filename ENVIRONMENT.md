Cloud Environment setup
------------

The ChatBot application is working in the cloud environment on the Google Cloud Platform (GCP) and
this document provides an overview of the currently configured environment.

## Cloud Run

We use [Cloud Run][cloud-run] as our main compute platform for the ChatBot application. 
Cloud Run is a managed serverless solution that works with Docker images and could scale 
the load when needed.

In Cloud Run we configure a `chat-bot-server` service with the 
`gcr.io/<projectName>/chat-bot-server` container image. The image is built automatically using
`jib` Gradle plugin and deployed to the [Container Registry][container-registry] for our needs 
as part of the build process.

Upon pushes to the `master` branch, the Cloud Build performs the automatic deployment of the 
new Cloud Run revision. (see [Cloud Build](#cloud-build) section for details).

[cloud-run]: https://cloud.google.com/run
[jib]: https://github.com/GoogleContainerTools/jib
[container-registry]: https://cloud.google.com/container-registry

## Cloud Build

We use [Cloud Build] CI/CD solution to continuously build and deploy our application.

The Cloud Build configuration is available as [`cloudbuild.yaml`](./cloudbuild.yaml) and does
the following:

1. Starts the Gradle build for the project.
2. Deploys the new revision of the Cloud Run service.

In addition to the configuration, we create a Cloud Build trigger to automatically start build
and deploy process upon commits to the `master` branch.

The Cloud Build itself uses GCP service accounts in order to access the APIs and should be 
configured to allow the Cloud Run deployment. See the [IAM](#iam) section for details.

[cloud-build]: https://cloud.google.com/cloud-build
[cloud-build-trigger]: https://cloud.google.com/cloud-build/docs/automating-builds/create-manage-triggers#console

## Hangouts Chat API

The bot uses [Hangout Chat API][chat-api] and is linked to the GCP project. Currently, it's only
possible to have a single bot per the GCP project.

The bot configuration could be done only via the web UI console as of the writing. It is published
in accordance to the publishing [guide][publishing-guide] where the essential configurations are: 

1. `Functionality` — check `Bot works in rooms`. We do not expect the bot to work in direct messages.
2. `Connection settings` — choose [`Cloud Pub/Sub`][pubsub-bot] and enter a Pub/Sub topic name 
    that'd be used to deliver messages from users to the bot. (for the Pub/Sub details see 
    [Pub/Sub](#pubsub) section)
3. `Permissions` — choose `Specific people and groups in your domain` and list individuals or
groups in the domain who'd be able to install the bot.

[chat-api]: https://developers.google.com/hangouts/chat
[publishing-guide]: https://developers.google.com/hangouts/chat/how-tos/bots-publish
[pubsub-bot]: https://developers.google.com/hangouts/chat/how-tos/pub-sub

## Pub/Sub

The application is built with resilience in mind and even though it exposes some REST APIs, 
it is not intended to handle to load directly. Instead, we rely on the Google [Pub/Sub][pubsub] 
async messaging service to receive the incoming messages and then stream the into the app.

At the moment of writing, the app requires the following Pub/Sub topics to be configured:

1. `incoming-bot-messages` — that is the topic that is used in the `Connection settings` of the
   bot configuration. The Hangouts Chat system takes care of propagating users messages to this
   topic.
   
   For the topic we configure the `incoming-bot-messages-cloud-run` push subscription that 
   delivers messages to the `/chat/incoming/event` endpoint of the Cloud Run [service](#cloud-run). 
   We configure The subscription with a backoff retry policy and the acknowledgement 
   deadline of 600 seconds.
   
   Also, the `dead lettering` is configured for the subscription, so all the undelivered
   messages are sent to our next topic — `dead-incoming-bot-messages`.
   
   The subscription uses `cloud-run-pubsub-invoker` service account to implement service2service
   authentication (see [IAM](#iam) section for details).

2. `dead-incoming-bot-messages` — the topic that holds possible undelivered incoming messages.
   
   For the topic we configure the `dead-incoming-bot-messages` pull never-expired subscription.
   In case of an undelivered message, one could go and pull it from the subscription. 

3. `repository-checks` — the topic that delivers scheduled tasks to check the watched resources
   build state (see [Cloud Scheduler](#cloud-scheduler) section for details)
   
   For the topic we configure the `repository-checks-cloud-run` subscription that delivers messages
   to the `/repositories/builds/check` endpoint of the Cloud Run [service](#cloud-run). 
   The subscription uses the same `cloud-run-pubsub-invoker` service account as the 
   `incoming-bot-messages-cloud-run` (see [IAM](#iam) section for details).

[pubsub]: https://cloud.google.com/pubsub

## Cloud Scheduler

For the CRON-based needs we rely on the [Cloud Scheduler][scheduler] service.

The scheduler service allows to configure multiple scheduled tasks that can deliver the task
payload to a particular target (HTTP endpoint, Pub/Sub topic or AppEngine endpoint).

For our needs, we configure a single CRON task `repositories-check-trigger` that emits 
a Pub/Sub message with an empty payload to the `repository-checks` topic. We configure the task
to run every hour using the following unix-cron format expression: `0 * * * *`.

[scheduler]: https://cloud.google.com/scheduler

## Secret Manager

To be able to use API tokens and service accounts securely in the application, 
we use [Secret Manager][secret-manager] service that allows managing and rotate secrets with ease.

We [manage][managing-secrets] secrets using the Secret Manager Web UI, but in order to be able 
to [read][reading-secrets] the secrets, developers and service accounts should have the
`roles/secretmanager.viewer` role that is not available by default (see [IAM](#iam) section 
for details).

For the application we configure the following secrets:

1. `ChatServiceAccount` — the private key of the chatbot actor that is used by the 
   [Hangouts Chat API](#hangouts-chat-api).
   
   The key is stored in JSON format as string value.
   
2. `TravisApiToken` — the Travis CI API token.
   
   The API token is used to authenticate calls to the Travis CI v3 API.

[secret-manager]: https://cloud.google.com/secret-manager
[managing-secrets]: https://cloud.google.com/secret-manager/docs/managing-secrets
[reading-secrets]: https://cloud.google.com/secret-manager/docs/managing-secret-versions#get

## IAM

Google [Cloud Identity and Access Management][iam] (IAM) lets us to fine-tune the authorization and 
access management for the application.

In order to run the application we configure the following service account and their respective
roles:

1. `chat-api-push@system.gserviceaccount.com` — a special Chat API service account used by the
    Chat to publish messages to the Pub/Sub topic.
    
    We [must][grant-publish-rights] assign the [`Pub/Sub Publisher`][publisher-role] role 
    to the service account in order to grant the Chat permission to publish user messages 
    to the defined topic.
    
2. `spine-chat-bot-actor@<projectName>.iam.gserviceaccount.com` — a custom service account we're 
    using as the credentials for the Chat API used by the bot. 
    
    The Hangouts Chat API works only with dedicated service account keys and 
    [could not][chat-api-with-default-sa] be used with default credentials. The API itself does
    not require any specific AIM role, but during the authentication the 
    [`chat.bot`][applying-chatbot-credentials] scope should be set.
    
3. `<projectId>-compute@developer.gserviceaccount.com` — default compute service account used by 
    Cloud Run.
    
    While we are relying on the default compute service account, virtually any service account 
    should work. In order to use the [Secret Manager](#secret-manager) API the service account 
    should have the secret `Secret Manager Viewer` role applied.
    
4. `<projectId>@cloudbuild.gserviceaccount.com` — [Cloud Build](#cloud-build) service account 
    used by the Cloud Build service to build and deploy the application.
    
    The Cloud Build service account is configured to act as a `Service Account User` and should
    have the `Cloud Run Admin` role in order to be able to [deploy][cloud-build-deploy-cloud-run] 
    the application.
    
5. `cloud-run-pubsub-invoker@<projectName>.iam.gserviceaccount.com` — a custom service account we're
    using to call the Cloud Run service from the [Pub/Sub](#pubsub) subscriptions.
    
    The Cloud Run is not accepting unauthenticated calls by default and is not exposed 
    to the internet. In order to be able to call the service, one 
    [must][cloud-run-service-to-service-auth] have the `Cloud Run Invoker` role.

[iam]: https://cloud.google.com/iam
[grant-publish-rights]: https://developers.google.com/hangouts/chat/how-tos/pub-sub#grant_publish_rights_on_your_topic
[publisher-role]: https://cloud.google.com/pubsub/docs/access-control#roles
[chat-api-with-default-sa]: https://stackoverflow.com/questions/62571412/hangout-chat-api-authentication-fails-with-default-service-account
[applying-chatbot-credentials]: https://developers.google.com/hangouts/chat/how-tos/service-accounts#step_2_applying_credentials_to_http_request_headers
[cloud-build-deploy-cloud-run]: https://cloud.google.com/cloud-build/docs/deploying-builds/deploy-cloud-run
[cloud-run-service-to-service-auth]: https://cloud.google.com/run/docs/authenticating/service-to-service
