/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

plugins {
    application
    id("com.github.johnrengelman.shadow")
    id("com.google.cloud.tools.jib")
    id("io.spine.tools.gradle.bootstrap")
}

/** The GCP project ID used for deployment of the application. **/
val gcpProject: String by project

spine {
    enableJava().server()
}

dependencies {
    annotationProcessor(enforcedPlatform(Deps.build.micronaut.bom))
    annotationProcessor(Deps.build.micronaut.injectJava)
    annotationProcessor(Deps.build.micronaut.validation)

    compileOnly(enforcedPlatform(Deps.build.micronaut.bom))

    implementation(enforcedPlatform(Deps.build.micronaut.bom))
    implementation(Deps.build.micronaut.inject)
    implementation(Deps.build.micronaut.validation)
    implementation(Deps.build.micronaut.runtime)
    implementation(Deps.build.micronaut.netty)
    implementation(Deps.build.micronaut.annotationApi)

    implementation(Deps.build.log4j2.core)
    runtimeOnly(Deps.build.log4j2.api)
    runtimeOnly(Deps.build.log4j2.slf4jBridge)

    implementation("io.spine.gcloud:spine-datastore:${Deps.versions.spineGcloud}")
    implementation(Deps.build.google.secretManager)
    implementation(Deps.build.google.pubsubProto)

    implementation(Deps.build.google.chat)
    implementation(Deps.build.google.auth)

    testAnnotationProcessor(enforcedPlatform(Deps.build.micronaut.bom))
    testAnnotationProcessor(Deps.build.micronaut.injectJava)

    testImplementation("io.spine:spine-testutil-server:${spine.version()}")
    testImplementation(enforcedPlatform(Deps.build.micronaut.bom))
    testImplementation(Deps.build.micronaut.testJUnit5)
    testImplementation(Deps.build.micronaut.httpClient)
}

application {
    mainClassName = "io.spine.chatbot.Application"
}

jib {
    to {
        image = "gcr.io/${gcpProject}/chat-bot-server"
        tags = setOf("latest")
    }
    container {
        mainClass = application.mainClassName
    }
}
