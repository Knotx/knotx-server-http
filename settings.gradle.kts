/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pluginManagement {
    val version: String by settings
    plugins {
        id("io.knotx.java-library") version version
        id("io.knotx.codegen") version version
        id("io.knotx.codegen-test") version version
        id("io.knotx.unit-test") version version
        id("io.knotx.jacoco") version version
        id("io.knotx.maven-publish") version version
        id("io.knotx.composite-build-support") version version
        id("io.knotx.release-java") version version
        id("org.nosphere.apache.rat") version "0.6.0"
    }
    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
    }
}

rootProject.name = "knotx-server-http"
include("knotx-server-http-api")
include("knotx-server-http-common-placeholders")
include("knotx-server-http-core")

project(":knotx-server-http-api").projectDir = file("api")
project(":knotx-server-http-common-placeholders").projectDir = file("common/placeholders")
project(":knotx-server-http-core").projectDir = file("core")