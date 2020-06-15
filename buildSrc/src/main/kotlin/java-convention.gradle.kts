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

import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    id("net.ltgt.errorprone")
    id("net.ltgt.apt-idea")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    errorprone(Deps.build.errorProneCore)
    implementation(Deps.build.guava)
    implementation(Deps.build.jsr305Annotations)
    implementation(Deps.build.checkerAnnotations)
    Deps.build.errorProneAnnotations.forEach { implementation(it) }

    testImplementation(Deps.test.guavaTestlib)
    Deps.test.junit5Api.forEach { testImplementation(it) }
    Deps.test.truth.forEach { testImplementation(it) }
    testRuntimeOnly(Deps.test.junit5Runner)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

tasks.compileJava {
    options.errorprone.disableWarningsInGeneratedCode.set(true)
}