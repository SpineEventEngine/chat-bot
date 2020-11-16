Spine ChatBot
-------

The ChatBot application is a Spine-based Google Chat [bot][chatbot-concepts] that
monitors the build statuses of Spine repositories and notifies the developers
via the [Google Chat][google-chat].

[chatbot-concepts]: https://developers.google.com/hangouts/chat/concepts/bots
[google-chat]: https://chat.google.com/

# Prerequisites

* [JDK 11][jdk11] or newer.
* [Docker SE][docker] v19.03 or newer.

[docker]: https://docs.docker.com/get-docker/
[jdk11]: https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html 

# Build

In order to build the application, run:

```bash
./gradlew clean build
```

Also, it is possible to build a Docker image using [`jib`][jib]:

```bash
./gradlew clean build jib
```

[jib]: https://github.com/GoogleContainerTools/jib

## GraalVM Native Image

In order to build a Native Image, one must have [GraalVM][graalvm] installed and 
[configured][graalvm-configuration].

One can use the snippet below to install GraalVM and it's Native Image plugin:

```bash
GRAALVM_VERSION="20.2.0"
JAVA_VERSION="11"
GRAALVM="graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAALVM_VERSION}"
GRAALVM_ARCHIVE="${GRAALVM}.tar.gz"
GRAALVM_JAVA="graalvm-ce-java${JAVA_VERSION}-${GRAALVM_VERSION}"

wget "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAALVM_VERSION}/${GRAALVM_ARCHIVE}"

tar -xvzf "${GRAALVM_ARCHIVE}"

sudo mkdir /usr/lib/jvm
sudo mv "${GRAALVM_JAVA}" /usr/lib/jvm
rm "${GRAALVM_ARCHIVE}"


JAVA_SDK_PATH="/usr/lib/jvm/${GRAALVM_JAVA}"
sudo bash -c "echo \"export JAVA_HOME=${JAVA_SDK_PATH}\" >> /etc/profile.d/graalvm.sh"
sudo bash -c "echo \"export PATH=${JAVA_SDK_PATH}/bin:\\\$PATH\" >> /etc/profile.d/graalvm.sh"

source /etc/profile.d/graalvm.sh

## Install Native Image using GVM `gu` utility

gu install native-image
```

With GraalVM installed and configured, run the Native Image compilation using the following 
Gradle command:

```bash
./gradlew clean build nativeImage
```


[graalvm]: https://www.graalvm.org
[graalvm-configuration]: https://www.graalvm.org/docs/getting-started-with-graalvm/linux/

# Running locally 

It is possible to run the application as a docker image locally. In order to do that, please make
sure you have saved the appropriate GCP credentials at `.credentials/gcp-adc.json`.

Then run the following script from the repository root folder:

```bash
export APP_PORT=8080
export LOCAL_PORT=9090
export CONTAINER_CREDENTIALS_PATH="/tmp/keys/gcp-adc.json"
export LOCAL_CREDENTIALS_PATH="${PWD}/.credentials/gcp-adc.json"
export GCP_PROJECT_ID="spine-chat-bot"
docker run \
    --tty \
    --rm \
    -p "${LOCAL_PORT}:${APP_PORT}" \
    -e "PORT=${APP_PORT}" \
    -e "MICRONAUT_SERVER_PORT=${APP_PORT}" \
    -e "GOOGLE_APPLICATION_CREDENTIALS=${CONTAINER_CREDENTIALS_PATH}" \
    -e "GCP_PROJECT_ID=${GCP_PROJECT_ID}" \
    -v "${LOCAL_CREDENTIALS_PATH}:${CONTAINER_CREDENTIALS_PATH}" \
    gcr.io/${GCP_PROJECT_ID}/chat-bot-server
```

The application will be available at `127.0.0.1:${LOCAL_PORT}` (e.g. `127.0.0.1:9090`). 
Locally-supplied GCP credentials are mounted into the image directly.

For detailed Application Default Credentials (ADC) guide for Docker see example 
Cloud Run [guide][cloud-run-local-guide].

[cloud-run-local-guide]: https://cloud.google.com/run/docs/testing/local#running_locally_using_docker_with_access_to_services

# Running in the Cloud

The application is deployed in the Google Cloud Platform cloud, and the overview of the 
cloud deployment is available in a separate [document](ENVIRONMENT.md).
