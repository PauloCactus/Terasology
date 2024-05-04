// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
    id("terasology-common")
}

apply(from = "$rootDir/config/gradle/common.gradle")

configure<SourceSetContainer> {
    // Adjust output path (changed with the Gradle 6 upgrade, this puts it back)
    main { java.destinationDirectory.set(layout.buildDirectory.dir("classes")) }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

dependencies {
    implementation(project(":engine"))
    api("com.jagrosh:DiscordIPC:_")

    constraints {
        // Upgrades for old transitive dependencies of DiscordIPC that Checkmarx doesn't like
        implementation("com.kohlschutter.junixsocket:junixsocket-common:_")
        implementation("com.kohlschutter.junixsocket:junixsocket-native-common:_")
        implementation("org.json:json:_")
    }
}
