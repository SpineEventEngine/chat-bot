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

import com.google.protobuf.gradle.protoc
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Spine
import org.gradle.plugins.ide.idea.model.Module
import org.gradle.plugins.ide.idea.model.ModuleLibrary

plugins {
    `java-library`
    idea
    id("com.google.protobuf")
    id("io.spine.mc-java")
}


val generatedRootDir = "${projectDir}/generated"
val generatedSpineDir = file("${generatedRootDir}/main/spine")
val generatedTestSpineDir = file("${generatedRootDir}/test/spine")

sourceSets {
    main {
        java {
            srcDir(generatedSpineDir)
        }
    }
    test {
        java {
            srcDir(generatedTestSpineDir)
        }
    }
}

idea {
    module {
        sourceDirs.add(generatedSpineDir)
        generatedSourceDirs.add(generatedSpineDir)
        testSourceDirs.add(generatedTestSpineDir)
        iml {
            beforeMerged(Action<Module> {
                dependencies.clear()
            })
            whenMerged(Action<Module> {
                dependencies.forEach {
                    (it as ModuleLibrary).isExported = true
                }
            })
        }
    }
}

dependencies {
    Protobuf.libs.forEach { implementation(it) }
    implementation(Spine.base)
    implementation(Spine.baseTypes)
    testImplementation(Spine.Test.base)
}

protobuf.protobuf.protoc {
    artifact = Protobuf.compiler
}