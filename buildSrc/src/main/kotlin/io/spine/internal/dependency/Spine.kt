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

package io.spine.internal.dependency

// https://github.com/SpineEventEngine
object Spine {
    private const val baseVersion = "2.0.0-SNAPSHOT.47"
    private const val baseTypesVersion = "2.0.0-SNAPSHOT.40"
    private const val coreVersion = "2.0.0-SNAPSHOT.47"
    private const val timeVersion = "2.0.0-SNAPSHOT.40"
    private const val gcloudVersion = "2.0.0-SNAPSHOT.4"

    const val base = "io.spine:spine-base:${baseVersion}"
    const val baseTypes = "io.spine:spine-base-types:${baseTypesVersion}"
    const val client = "io.spine:spine-client:${coreVersion}"
    const val server = "io.spine:spine-server:${coreVersion}"
    const val serverProto = "io.spine:spine-server:${coreVersion}:proto"
    const val core = "io.spine:spine-core:${coreVersion}"
    const val datastore = "io.spine.gcloud:spine-datastore:${gcloudVersion}"
    const val pubsub = "io.spine.gcloud:spine-pubsub:${gcloudVersion}"
    const val time = "io.spine:spine-time:${timeVersion}"

    object Test {
        const val base = "io.spine.tools:spine-testlib:${baseVersion}"
        const val client = "io.spine.tools:spine-testutil-client:${coreVersion}"
        const val server = "io.spine.tools:spine-testutil-server:${coreVersion}"
        const val core = "io.spine.tools:spine-testutil-core:${coreVersion}"
        const val time = "io.spine:spine-testutil-time:${timeVersion}"
    }

    object Stable {

        const val version = "1.7.4"
        const val coreVersion = "1.7.5"
        const val timeVersion = "1.7.1"
        const val gcloudVersion = "1.7.1"

        const val base = "io.spine:spine-base:${version}"
        const val client = "io.spine:spine-client:${coreVersion}"
        const val server = "io.spine:spine-server:${coreVersion}"
        const val core = "io.spine:spine-core:${coreVersion}"
        const val time = "io.spine:spine-time:${timeVersion}"
        const val datastore = "io.spine.gcloud:spine-datastore:${gcloudVersion}"
        const val pubsub = "io.spine.gcloud:spine-pubsub:${gcloudVersion}"

        object Test {
            const val base = "io.spine:spine-testlib:${version}"
            const val client = "io.spine:spine-testutil-client:${coreVersion}"
            const val server = "io.spine:spine-testutil-server:${coreVersion}"
            const val core = "io.spine:spine-testutil-core:${coreVersion}"
            const val time = "io.spine:spine-testutil-time:${timeVersion}"
        }
    }
}
