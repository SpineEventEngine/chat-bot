/*
 * Copyright 2021, TeamDev. All rights reserved.
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
import io.spine.internal.dependency.Flogger
import io.spine.internal.dependency.Gcp
import io.spine.internal.dependency.Log4j2
import io.spine.internal.dependency.Micronaut
import io.spine.internal.dependency.Spine

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.google.cloud.tools.jib")
    id("io.micronaut.application")
    spine
}

val extras by extra(io.spine.internal.gradle.prepareExtras(project))

micronaut {
    runtime(MicronautRuntime.NETTY)
    testRuntime(MicronautTestRuntime.JUNIT_5)
    version.set(Micronaut.version)
    processing {
        incremental.set(true)
        annotations.add("io.spine.chatbot")
    }
}

dependencies {
    implementation(Micronaut.netty)
    implementation(Micronaut.annotationApi)
    implementation(Micronaut.validation)
    implementation(Micronaut.runtime)

    runtimeOnly(Log4j2.core)
    runtimeOnly(Log4j2.api)
    runtimeOnly(Log4j2.slf4jBridge)
    runtimeOnly(Log4j2.jclBridge)
    runtimeOnly(Log4j2.julBridge)
    runtimeOnly(Flogger.Runtime.log4J2) {
        exclude("org.apache.logging.log4j:log4j-api")
        exclude("org.apache.logging.log4j:log4j-core")
    }
    implementation(Flogger.lib)

    implementation(Spine.server)
    implementation(Spine.datastore)
    implementation(Spine.pubsub)

    implementation(Gcp.secretManager)

    implementation(Gcp.chat)
    implementation(Gcp.auth)

    testImplementation(Micronaut.testJUnit5)
    testImplementation(Micronaut.httpClient)
    testImplementation(Spine.Test.server)
}

val appClassName = "io.spine.chatbot.Application"
project.setProperty("mainClassName", appClassName)

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    mergeServiceFiles("desc.ref")
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
        attributes["Main-Class"] = appClassName
    }
}

application {
    mainClass.set(appClassName)
    applicationDefaultJvmArgs = listOf(
        "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=7007"
    )
}

jib {
    from {
        image = "amazoncorretto:16"
    }
    to {
        image = "gcr.io/${extras.gcpProject}/chat-bot-server"
        tags = setOf("latest", extras.git.hash, extras.git.shortHash, "v${version}")
    }
    container {
        mainClass = appClassName
    }
}
