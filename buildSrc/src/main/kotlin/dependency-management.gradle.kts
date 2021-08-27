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

import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.FindBugs
import io.spine.internal.dependency.Flogger
import io.spine.internal.dependency.Gson
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Log4j2
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Spine
import io.spine.internal.dependency.Truth

plugins {
    `java-library`
}

repositories {
    mavenCentral()
    google()
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

configurations.all {
    resolutionStrategy {
        force(
            CheckerFramework.annotations,
            ErrorProne.annotations,
            Guava.lib,
            Guava.testLib,
            FindBugs.annotations,
            Gson.lib,
            JavaX.annotations,
            Protobuf.libs,
            Truth.libs,
            Spine.base,
            Spine.baseTypes,
            Spine.core,
            Spine.server,
            Spine.client,
            Spine.time,
            Spine.Test.base,
            Spine.Test.core,
            Spine.Test.server,
            Spine.Test.client,
            Spine.Test.time,
            Flogger.lib,
            Log4j2.slf4jBridge,
            Log4j2.api,
            Log4j2.core,
            Log4j2.julBridge,
            Log4j2.jclBridge,
            "org.slf4j:slf4j-api:2.0.0-alpha2"
        )
    }
    exclude("com.google.guava", "guava-jdk5")
    exclude("org.slf4j", "slf4j-jdk14")
}
