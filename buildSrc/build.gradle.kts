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

plugins {
    `kotlin-dsl`
}


repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://spine.mycloudrepo.io/public/repositories/releases") {
        content {
            includeGroup("io.spine")
            includeGroup("io.spine.tools")
            includeGroup("io.spine.gcloud")
        }
        mavenContent {
            releasesOnly()
        }
    }
    maven("https://spine.mycloudrepo.io/public/repositories/snapshots")
    maven {
        setUrl("https://maven.pkg.github.com/SpineEventEngine/*")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

val spineBaseVersion = "2.0.0-SNAPSHOT.47"

dependencies {
    implementation("io.spine.tools:spine-mc-java:${spineBaseVersion}")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
    implementation("gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:3.1.2")
    implementation("net.saliman:gradle-properties-plugin:1.5.1")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:1.5.4")
}
