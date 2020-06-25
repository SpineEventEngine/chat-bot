Cloud Environment setup
------------

The ChatBot application is working in the cloud environment on the Google Cloud Platform (GCP) and
this document provides an overview of the currently configured environment.

## Cloud Run

We use [Cloud Run][cloud-run] as our main compute platform for the ChatBot application. 
Cloud Run is a managed serverless solution that works with Docker images and could scale 
the load when needed.

In Cloud Run we configure a `chat-bot-server` service with the 
`gcr.io/<project-name>/chat-bot-server` container image. The image is built automatically using
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

//TODO:2020-06-23:ysergiichuk: add IAM section.
