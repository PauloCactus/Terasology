// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
    id("terasology-common")
}

apply(from = "$rootDir/config/gradle/publish.gradle")

group = "org.terasology.subsystems"
version = project(":engine").version

configure<SourceSetContainer> {
    // Adjust output path (changed with the Gradle 6 upgrade, this puts it back)
    main { java.destinationDirectory.set(layout.buildDirectory.dir("classes")) }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

dependencies {
    implementation("org.slf4j:slf4j-api:_")
    implementation("net.sf.trove4j:trove4j:_")

    implementation("org.terasology:reflections:_")
    implementation("org.terasology.nui:nui-reflect:_")
    implementation("org.terasology.gestalt:gestalt-module:_")
    implementation("org.terasology.gestalt:gestalt-asset-core:_")

    testRuntimeOnly("org.slf4j:slf4j-simple:_") {
        because("log output during tests")
    }
    testImplementation(platform(Testing.junit.bom)) {
        // junit-bom will set version numbers for the other org.junit dependencies.
    }
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.params)
    testRuntimeOnly(Testing.junit.jupiter.engine)
    testImplementation(Testing.mockito.inline)

    testImplementation(Testing.mockito.junitJupiter)
}

tasks.register<Test>("unitTest") {
    group = "Verification"
    description = "Runs unit tests (fast)"

    useJUnitPlatform {
        excludeTags("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}
