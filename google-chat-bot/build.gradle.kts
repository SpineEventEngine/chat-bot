/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.micronaut.gradle.MicronautRuntime
import io.micronaut.gradle.MicronautTestRuntime
import io.micronaut.gradle.graalvm.NativeImageTask

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.google.cloud.tools.jib")
    id("io.spine.tools.gradle.bootstrap")
    id("io.micronaut.application")
}

/** The GCP project ID used for deployment of the application. **/
val gcpProject: String by project

spine {
    enableJava().server()
}

micronaut {
    runtime(MicronautRuntime.NETTY)
    testRuntime(MicronautTestRuntime.JUNIT_5)
    enableNativeImage.set(true)
    version.set(Deps.versions.micronaut)
    processing {
        incremental.set(true)
        annotations.add("io.spine.chatbot")
    }
}

/**
 * The following configs are required in order to start building the Native Image.
 *
 * <p>The further improvements to the Spine framework are required due to the usage
 * of currently non-supported reflection API within the GraalVM. E.g. the {@code invoke} or
 * the {@code MethodHandles} usage within the {@code io.spine.server.entity.storage.Scanner}.
 *
 * <p>See https://www.graalvm.org/reference-manual/native-image/Limitations for details.
 */
tasks.withType<NativeImageTask>().apply {
    forEach {
        it.args(
                "-H:+TraceClassInitialization",
                "-Dlog4j2.disable.jmx=true",
                "--initialize-at-build-time=org.apache.logging.log4j",
                "--initialize-at-build-time=org.apache.logging.slf4j",
                "--initialize-at-build-time=com.sun.jmx.defaults",
                "--initialize-at-build-time=com.sun.jmx.mbeanserver",
                "--initialize-at-build-time=com.sun.org.apache.xerces",
                "--initialize-at-build-time=jdk.management.jfr",
                "--initialize-at-build-time=com.google.protobuf",
                "--initialize-at-build-time=com.google.gson",
                "--initialize-at-build-time=org.conscrypt",
                "--initialize-at-build-time=java.beans.Introspector",
                "--verbose"
        )
    }
}

dependencies {
    compileOnly(Deps.build.graalvmSvm)

    implementation(Deps.build.micronaut.netty)
    implementation(Deps.build.micronaut.annotationApi)
    implementation(Deps.build.micronaut.validation)
    implementation(Deps.build.micronaut.runtime)

    implementation(Deps.build.log4j2.core)
    runtimeOnly(Deps.build.log4j2.api)
    runtimeOnly(Deps.build.log4j2.slf4jBridge)
    implementation(Deps.build.log4j2.floggerBackend) {
        exclude("org.apache.logging.log4j:log4j-api")
        exclude("org.apache.logging.log4j:log4j-core")
    }
    implementation(Deps.build.flogger)

    implementation(Deps.build.spine.datastore)
    implementation(Deps.build.spine.pubsub)

    implementation(Deps.build.google.secretManager)

    implementation(Deps.build.google.chat)
    implementation(Deps.build.google.auth)

    testImplementation(Deps.build.spine.serverTestUtil)
    testImplementation(Deps.build.micronaut.testJUnit5)
    testImplementation(Deps.build.micronaut.httpClient)
}

val shadowJar: ShadowJar by tasks
shadowJar.apply {
    mergeServiceFiles()
    mergeServiceFiles("desc.ref")
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}

application {
    mainClass.set("io.spine.chatbot.Application")
}

jib {
    to {
        image = "gcr.io/${gcpProject}/chat-bot-server"
        tags = setOf("latest")
    }
    container {
        mainClass = application.mainClass.get()
    }
}
