Cloud Environment setup
------------

The ChatBot application is working in the cloud environment on the Google Cloud Platform (GCP) and
this document provides an overview of the currently configured environment.

## Cloud Run

The [Cloud Run][cloud-run] is used as the main compute platform for the ChatBot application. 
Cloud Run is a managed serverless solution that works with Docker images and is able to scale
upon the load when needed.

The `chat-bot-server` service is configured in Cloud Run with the 
`gcr.io/<projectName>/chat-bot-server` container image. The image is built automatically using
[`jib`][jib] Gradle plugin and deployed to the [Container Registry][container-registry] as part 
of the build process.

Upon pushes to the `master` branch, the Cloud Build performs the automatic deployment of the 
new Cloud Run revision (see [Cloud Build](#cloud-build) section for details).

[cloud-run]: https://cloud.google.com/run
[jib]: https://github.com/GoogleContainerTools/jib
[container-registry]: https://cloud.google.com/container-registry

## Cloud Build

The [Cloud Build] CI/CD solution is used to continuously build and deploy the application.

The Cloud Build configuration is available as [`cloudbuild.yaml`](./cloudbuild.yaml) and does
the following:

1. Starts the Gradle build for the project.
2. Deploys the new revision of the Cloud Run service.

In addition to the configuration, the Cloud Build trigger is configured to automatically start build
and deploy process upon commits to the `master` branch. In order to allow Cloud Build to 
fetch code from the GitHub, the Cloud Build GitHub [application][cloud-build-github-app] 
is [configured][run-builds-on-github] for the organization.

The Cloud Build itself uses GCP service accounts in order to access the APIs and should be 
configured to allow the Cloud Run deployment (see the [IAM](#iam) section for details).

[cloud-build]: https://cloud.google.com/cloud-build
[cloud-build-trigger]: https://cloud.google.com/cloud-build/docs/automating-builds/create-manage-triggers#console
[cloud-build-github-app]: https://github.com/marketplace/google-cloud-build
[run-builds-on-github]: https://cloud.google.com/cloud-build/docs/automating-builds/run-builds-on-github

## Hangouts Chat API

The bot uses [Hangout Chat API][chat-api] and is linked to the GCP project. Currently, it's only
possible to have a single bot per the GCP project.

The bot configuration is only available via the web UI console. The bot is published in accordance 
to the publishing [guide][publishing-guide] where the essential configurations are: 

1. `Functionality` — check `Bot works in rooms`. The bot is not expected to work in direct messages.
2. `Connection settings` — choose [`Cloud Pub/Sub`][pubsub-bot] and enter a Pub/Sub topic name 
    that'd be used to deliver messages from users to the bot (for the Pub/Sub details see 
    [Pub/Sub](#pubsub) section).
3. `Permissions` — choose `Specific people and groups in your domain` and list individuals or
groups in the domain who'd be able to install the bot.

[chat-api]: https://developers.google.com/hangouts/chat
[publishing-guide]: https://developers.google.com/hangouts/chat/how-tos/bots-publish
[pubsub-bot]: https://developers.google.com/hangouts/chat/how-tos/pub-sub

## Pub/Sub

The application is built with resilience in mind and even though it exposes some REST APIs, 
it is not intended to handle to load directly. Instead, it relies on the Google [Pub/Sub][pubsub] 
async messaging service to receive the incoming messages and then stream them into the app.

The bot requires the following Pub/Sub topics to be configured:

1. `incoming-bot-messages` — that is the topic that is used in the `Connection settings` of the
   bot configuration. The Hangouts Chat system takes care of propagating user messages to this
   topic.
   
   For the topic the `incoming-bot-messages-cloud-run` push subscription is created with 
   a backoff retry policy and the acknowledgement deadline of 600 seconds. 
   The subscription delivers messages to the `/chat/incoming/event` endpoint of 
   the Cloud Run [service](#cloud-run). 
   
   Also, the `dead lettering` is configured for the subscription, so all the undelivered
   messages are sent to `dead-incoming-bot-messages` topic.
   
   The subscription uses `cloud-run-pubsub-invoker` service account to implement service2service
   authentication (see [IAM](#iam) section for details).

2. `dead-incoming-bot-messages` — the topic that holds undelivered incoming messages.
   
   For the topic, the `dead-incoming-bot-messages` pull subscription that never expires 
   is configured.
   In case of an undelivered message, one could go and pull it from the subscription.

3. `repository-checks` — the topic that delivers scheduled tasks to check the build state of 
   the watched resources (see [Cloud Scheduler](#cloud-scheduler) section for details).
   
   The `repository-checks-cloud-run` subscription is configured for the topic. The subscription 
   delivers messages to the `/repositories/builds/check` endpoint of the Cloud Run 
   [service](#cloud-run). 
   The subscription uses the same `cloud-run-pubsub-invoker` service account as the 
   `incoming-bot-messages-cloud-run` (see [IAM](#iam) section for details).

[pubsub]: https://cloud.google.com/pubsub

## Cloud Scheduler

The [Cloud Scheduler][scheduler] service allows configuring multiple scheduled tasks that deliver 
the payload to a particular target (HTTP endpoint, Pub/Sub topic or AppEngine endpoint).

The CRON task `repositories-check-trigger` is configured for the bot. The task emits a Pub/Sub 
message with an empty payload to the `repository-checks` topic. It is 
[configured][configure-schedules] to run every hour using the following unix-cron format 
expression: `0 * * * *`.

[scheduler]: https://cloud.google.com/scheduler
[configure-schedules]: https://cloud.google.com/scheduler/docs/configuring/cron-job-schedules

## Secret Manager

The [Secret Manager][secret-manager] service is used to supply application secrets like API tokens
and service accounts securely.

The secreats are [managed][managing-secrets] the Secret Manager Web UI, but in order to be able 
to [read][reading-secrets] the secrets, developers and service accounts should have the
`roles/secretmanager.viewer` role that is not configured by default (see [IAM](#iam) section 
for details).

The following secrets are configured for the bot:

1. `ChatServiceAccount` — the private key of the chatbot actor that is used by the 
   [Hangouts Chat API](#hangouts-chat-api).
   
   The key is stored in JSON format as string value.
   
2. `TravisApiToken` — the Travis CI API token.
   
   The API token is used to authenticate calls to the Travis CI v3 API.

[secret-manager]: https://cloud.google.com/secret-manager
[managing-secrets]: https://cloud.google.com/secret-manager/docs/managing-secrets
[reading-secrets]: https://cloud.google.com/secret-manager/docs/managing-secret-versions#get

## IAM

The [Cloud Identity and Access Management][iam] (IAM) service is used to fine-tune the authorization 
and access management for the application.

In order to run the application following service accounts, and their respective roles 
are configured:

1. `chat-api-push@system.gserviceaccount.com` — a special Chat API service account used by the
    Chat to publish messages to the Pub/Sub topic.
    
    The [`Pub/Sub Publisher`][publisher-role] role [must][grant-publish-rights] be assigned
    to the service account in order to grant the Chat permission to publish user messages 
    to the defined topic.
    
2. `spine-chat-bot-actor@<projectName>.iam.gserviceaccount.com` — a custom service account that is 
    used as the credentials for the Chat API. 
    
    The Hangouts Chat API works only with dedicated service account keys and 
    [could not][chat-api-with-default-sa] be used with default credentials. The API itself does
    not require any specific AIM role, but during the authentication the 
    [`chat.bot`][applying-chatbot-credentials] scope should be set.
    
3. `<projectId>-compute@developer.gserviceaccount.com` — default service account used by Cloud Run.
    
    It is not required to set a custom service account for the Cloud Run, but in order to use 
    the [Secret Manager](#secret-manager) API the service account should have the 
    `Secret Manager Viewer` role applied.
    
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
