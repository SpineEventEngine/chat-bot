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
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Truth
import io.spine.internal.dependency.Gcp
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("net.ltgt.errorprone")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    errorprone(ErrorProne.core)
    errorprone(ErrorProne.Plugin.nullaway)
    compileOnlyApi(CheckerFramework.annotations)
    compileOnlyApi(FindBugs.annotations)
    compileOnlyApi(JavaX.annotations)
    ErrorProne.annotations.forEach { compileOnlyApi(it) }

    constraints {
        implementation(Guava.lib)
    }
    api(platform(Gcp.bom))
    implementation(Guava.lib)
    testImplementation(Guava.testLib)
    testImplementation(enforcedPlatform(JUnit.bom))
    JUnit.api.forEach { testImplementation(it) }
    Truth.libs.forEach { testImplementation(it) }
    testRuntimeOnly(JUnit.engine)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }

    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.compileJava {
    // Explicitly states the encoding of the source and test source files, ensuring
    // correct execution of the `javac` task.
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-Werror"))

    // Configure Error Prone:
    // 1. Exclude generated sources from being analyzed by Error Prone.
    // 2. Turn the check off until Error Prone can handle `@Nested` JUnit classes.
    //    See issue: https://github.com/google/error-prone/issues/956
    // 3. Turn off checks which report unused methods and unused method parameters.
    //    See issue: https://github.com/SpineEventEngine/config/issues/61
    //
    // For more config details see:
    //    https://github.com/tbroyer/gradle-errorprone-plugin/tree/master#usage
    options.errorprone.errorproneArgs.addAll(
        listOf(
            "-XepExcludedPaths:.*/generated/.*",
            "-Xep:ClassCanBeStatic:OFF",
            "-Xep:UnusedMethod:OFF",
            "-Xep:UnusedVariable:OFF",
            "-Xep:CheckReturnValue:OFF"
        )
    )

    options.errorprone {
        option("NullAway:AnnotatedPackages", "io.spine.chatbot")
        disableWarningsInGeneratedCode.set(true)
    }
}

tasks.compileTestJava {
    options.errorprone.isEnabled.set(false)
}
