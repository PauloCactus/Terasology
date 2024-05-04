// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import java.net.URI

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    google()  // gestalt uses an annotation package by Google
    gradlePluginPortal()

    maven {
        name = "Terasology Artifactory"
        url = URI("https://artifactory.terasology.io/artifactory/virtual-repo-live")
    }

    // TODO MYSTERY: As of November 7th 2021 virtual-repo-live could no longer be relied on for latest snapshots - Pro feature?
    // We've been using it that way for *years* and nothing likewise changed in the area for years as well. This seems to work ....
    maven {
        name = "Terasology snapshot locals"
        url = URI("https://artifactory.terasology.io/artifactory/terasology-snapshot-local")
    }
}

dependencies {
    implementation("org.terasology:reflections:_") {
        because("reflections-manifest.gradle.kts")
    }
    // Additional corrections for old reflections dependencies:
    constraints {
        implementation("com.google.guava:guava:_")
        implementation("org.javassist:javassist:_")
        implementation("net.bytebuddy:bytebuddy:_")
    }

    // graph analysis
    implementation("org.jgrapht:jgrapht-core:_")

    // for inspecting modules
    implementation("org.terasology.gestalt:gestalt-module:_")

    // plugins we configure
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:_")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:_")

    api(kotlin("test"))
}

group = "org.terasology.gradology"
