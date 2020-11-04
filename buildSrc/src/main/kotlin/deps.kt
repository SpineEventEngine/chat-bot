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

object Versions {
    const val checkerFramework = "3.7.1"
    const val errorProne = "2.4.0"
    const val pmd = "6.29.0"
    const val checkstyle = "8.37"
    const val findBugs = "3.0.2"
    const val guava = "30.0-jre"
    const val flogger = "0.5.1"
    const val junit5 = "5.7.0"
    const val truth = "1.1"
    const val micronaut = "2.2.1"
    const val spineGcloud = "1.6.6"
    const val spineBase = "1.6.6"
    const val spineCore = "1.6.6"
    const val googleSecretManager = "1.2.4"
    const val googleChat = "v1-rev20201019-1.30.10"
    const val googleAuth = "0.22.0"
    const val log4j2 = "2.13.3"
}

object Build {
    val errorProneAnnotations = listOf(
            "com.google.errorprone:error_prone_annotations:${Versions.errorProne}",
            "com.google.errorprone:error_prone_type_annotations:${Versions.errorProne}"
    )
    const val errorProneCheckApi = "com.google.errorprone:error_prone_check_api:${Versions.errorProne}"
    const val errorProneCore = "com.google.errorprone:error_prone_core:${Versions.errorProne}"
    const val errorProneTestHelpers = "com.google.errorprone:error_prone_test_helpers:${Versions.errorProne}"
    const val checkerAnnotations = "org.checkerframework:checker-qual:${Versions.checkerFramework}"
    val checkerDataflow = listOf(
            "org.checkerframework:dataflow:${Versions.checkerFramework}",
            "org.checkerframework:javacutil:${Versions.checkerFramework}"
    )
    const val jsr305Annotations = "com.google.code.findbugs:jsr305:${Versions.findBugs}"
    const val guava = "com.google.guava:guava:${Versions.guava}"
    const val flogger = "com.google.flogger:flogger:${Versions.flogger}"
    val ci = "true".equals(System.getenv("CI"))
    val micronaut = Micronaut
    val google = Google
    val log4j2 = Log4j2
    var spine = Spine
}

object Spine {
    const val datastore = "io.spine.gcloud:spine-datastore:${Versions.spineGcloud}"
    const val pubsub = "io.spine.gcloud:spine-pubsub:${Versions.spineGcloud}"
    const val base = "io.spine:spine-base:${Versions.spineBase}"
    const val server = "io.spine:spine-server:${Versions.spineCore}"
    const val client = "io.spine:spine-client:${Versions.spineCore}"
    const val core = "io.spine:spine-core:${Versions.spineCore}"
}

object Log4j2 {
    const val core = "org.apache.logging.log4j:log4j-core:${Versions.log4j2}"
    const val api = "org.apache.logging.log4j:log4j-api:${Versions.log4j2}"
    const val slf4jBridge = "org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j2}"
}

object Google {
    const val secretManager = "com.google.cloud:google-cloud-secretmanager:${Versions.googleSecretManager}"
    const val chat = "com.google.apis:google-api-services-chat:${Versions.googleChat}"
    const val auth = "com.google.auth:google-auth-library-oauth2-http:${Versions.googleAuth}"
}

object Micronaut {
    const val bom = "io.micronaut:micronaut-bom:${Versions.micronaut}"
    const val inject = "io.micronaut:micronaut-inject"
    const val injectJava = "io.micronaut:micronaut-inject-java"
    const val validation = "io.micronaut:micronaut-validation"
    const val runtime = "io.micronaut:micronaut-runtime"
    const val netty = "io.micronaut:micronaut-http-server-netty"
    const val testJUnit5 = "io.micronaut.test:micronaut-test-junit5"
    const val httpClient = "io.micronaut:micronaut-http-client"
    const val annotationApi = "javax.annotation:javax.annotation-api"
}

object Test {
    val junit5Api = listOf(
            "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}",
            "org.junit.jupiter:junit-jupiter-params:${Versions.junit5}"
    )
    const val junit5Runner = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
    const val guavaTestlib = "com.google.guava:guava-testlib:${Versions.guava}"
    val truth = listOf(
            "com.google.truth:truth:${Versions.truth}",
            "com.google.truth.extensions:truth-java8-extension:${Versions.truth}",
            "com.google.truth.extensions:truth-proto-extension:${Versions.truth}"
    )
}

object Deps {
    val build = Build
    val test = Test
    val versions = Versions
}
