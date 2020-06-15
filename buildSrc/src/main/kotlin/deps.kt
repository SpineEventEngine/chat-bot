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
    const val checkerFramework = "3.3.0"
    const val errorProne = "2.4.0"
    const val pmd = "6.20.0"
    const val checkstyle = "8.29"
    const val findBugs = "3.0.2"
    const val guava = "29.0-jre"
    const val grpc = "1.28.1"
    const val flogger = "0.5.1"
    const val junit5 = "5.6.2"
    const val junitPlatform = "1.6.2"
    const val truth = "1.0.1"
    const val micronaut = "2.0.0.M3"
    const val spineGcloud = "1.5.0"
    const val googleSecretManager = "1.0.1"
    const val googlePubsubProto = "1.89.0"
    const val googleChat = "v1-rev20200502-1.30.9"
    const val googleAuth = "0.20.0"

    const val licensePlugin = "1.14"
    const val errorPronePlugin = "1.2.1"
    const val aptPlugin = "0.21"
    const val shadowPlugin = "5.2.0"
    const val jibPlugin = "2.4.0"
    const val spineBootstrapPlugin = "1.5.17"
    const val propertiesPlugin = "1.5.1"
}

object GradlePlugins {
    const val apt = "net.ltgt.gradle:gradle-apt-plugin:${Versions.aptPlugin}"
    const val shadow = "com.github.jengelman.gradle.plugins:shadow:${Versions.shadowPlugin}"
    const val jib = "gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:${Versions.jibPlugin}"
    const val properties = "net.saliman:gradle-properties-plugin:${Versions.propertiesPlugin}"
    const val spineBootstrap = "io.spine.tools:spine-bootstrap:${Versions.spineBootstrapPlugin}"
    const val errorProne = "net.ltgt.gradle:gradle-errorprone-plugin:${Versions.errorPronePlugin}"
    const val licenseReport = "com.github.jk1:gradle-license-report:${Versions.licensePlugin}"
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
    val gradlePlugins = GradlePlugins
    val micronaut = Micronaut
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
}

object Runtime {

    val flogger = Flogger

    object Flogger {
        const val systemBackend = "com.google.flogger:flogger-system-backend:${Versions.flogger}"
        const val log4J = "com.google.flogger:flogger-log4j:${Versions.flogger}"
        const val slf4J = "com.google.flogger:slf4j-backend-factory:${Versions.flogger}"
    }
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
    val runtime = Runtime
    val test = Test
    val versions = Versions
}
