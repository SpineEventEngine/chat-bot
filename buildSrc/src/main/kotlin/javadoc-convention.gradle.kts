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

/*
 * This plugin enables the new Javadoc tags in the build.
 *
 * The tags are:
 * 1. @apiNote - the additional notes and commentaries regarding the API
 * 2. @implSpec - the implementation specification
 * 3. @implNote - the commentaries and notes about the implementation
 *
 * It also explicitly states the encoding of the source files from which the Javadoc is composed,
 * ensuring correct execution of the `javadoc` task.
 *
 * For the detailed description of the new tags, see:
 * https://blog.codefx.org/java/new-javadoc-tags/#apiNote-implSpec-and-implNote
 *
 * This script should be applied to the `subprojects` section of the root project,
 * or to the specific child projects.
 */

plugins {
    `java-library`
}

object JavadocOptions {

    const val encoding = "UTF-8"
    val tags = setOf(
        "apiNote:a:API Note:",
        "implSpec:a:Implementation Requirements:",
        "implNote:a:Implementation Note:"
    )
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).tags?.addAll(JavadocOptions.tags)
    (options as StandardJavadocDocletOptions).encoding = JavadocOptions.encoding
}

if (JavaVersion.current().isJava8Compatible) {
    tasks.javadoc {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

if (JavaVersion.current().isJava11Compatible) {
    tasks.javadoc {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
