pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.2"
}

stonecutter {
    create(rootProject) {
        // Fabric, obfuscated runtime (<26.1: remap against official Mojang mappings).
        listOf("1.21", "1.21.2", "1.21.5", "1.21.6", "1.21.9", "1.21.11").forEach { mc ->
            versions("$mc-fabric" to mc).buildscript("build.fabric-obf.gradle.kts")
        }
        // Fabric, deobfuscated runtime (Minecraft 26.1+ ships Mojang-mapped).
        listOf("26.1", "26.2").forEach { mc ->
            versions("$mc-fabric" to mc).buildscript("build.fabric-deobf.gradle.kts")
        }
        // NeoForge.
        listOf("1.21", "1.21.2", "1.21.5", "1.21.6", "1.21.9", "1.21.11", "26.1", "26.2").forEach { mc ->
            versions("$mc-neoforge" to mc).buildscript("build.neoforge.gradle.kts")
        }
        vcsVersion = "1.21.11-fabric"
    }
}
