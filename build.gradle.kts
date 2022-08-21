/*
 * MIT License
 *
 * Copyright (c) 2022 Frederick Baier & Philipp Eistrach
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

plugins {
    java
    id ("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.7.10"
}

allprojects {
    group = "app.simplecloud"
    version = "1.0.0-SNAPSHOT"

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        mavenCentral()
        maven("https://repo.thesimplecloud.eu/artifactory/list/gradle-release-local/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
    }
}

subprojects {

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("com.google.guava:guava:31.1-jre")
        testImplementation(platform("org.junit:junit-bom:5.9.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
