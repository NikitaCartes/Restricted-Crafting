plugins {
    id("java")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

// Tag this node's loader and version so [fabric."x"] keys resolve via bare property("...").
stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

repositories {
    mavenCentral()
}

base.archivesName = "${property("mod_id")}-fabric-mc${property("minecraft_version")}"
version = property("mod_version").toString()

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

loom {
    serverOnlyMinecraftJar()
}

dependencies {
    // Fabric (26.1+ ships Mojang-mapped, so no `mappings(...)` line is needed)
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    implementation(fabricApi.module("fabric-lifecycle-events-v1", property("fabric_version").toString()))
    implementation(fabricApi.module("fabric-command-api-v2", property("fabric_version").toString()))
    implementation(fabricApi.module("fabric-api-base", property("fabric_version").toString()))

    // LuckPerms API (provided at runtime by the LuckPerms mod)
    compileOnly("net.luckperms:api:${property("luckperms_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    from("LICENSE")
}

val modExpansions = mapOf(
    "version" to project.version.toString(),
    "supported_minecraft_version" to property("supported_minecraft_version").toString()
)

tasks.processResources {
    inputs.properties(modExpansions)
    filesMatching("fabric.mod.json") { expand(modExpansions) }

    // Deobfuscated (Mojang-mapped) runtime needs no mixin refmap.
    filesMatching("restricted-crafting.mixins.json") {
        filter { it.replace("\"refmap\": \"\${refmap}\",", "") }
    }
    exclude("META-INF/neoforge.mods.toml")
}

tasks.register<Copy>("collectJars") {
    group = "build"
    from(tasks.jar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build", rootProject.tasks.named("cleanCollectedJars"))
}

publishMods {
    val modrinthToken = System.getenv("MODRINTH_TOKEN") ?: ""
    val curseforgeToken = System.getenv("CURSEFORGE_TOKEN") ?: ""
    val githubToken = System.getenv("GITHUB_TOKEN") ?: ""
    file = tasks.jar.get().archiveFile
    dryRun = modrinthToken.isEmpty() || curseforgeToken.isEmpty() || githubToken.isEmpty()
    displayName = "${property("display_name")} ${project.version}"
    version = project.version.toString()
    changelog = rootProject.file("RELEASE_NOTE.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    val targets = property("supported_versions").toString().split(",")
    modrinth {
        projectId = "Tf9ijCH5"
        accessToken = modrinthToken
        targets.forEach(minecraftVersions::add)
        requires("luckperms")
    }
    curseforge {
        projectId = "1278389"
        accessToken = curseforgeToken
        targets.forEach(minecraftVersions::add)
        requires("luckperms")
        server.set(true)
    }
    // Uploads this node's jar into the single release created by the root publishGithub task.
    github {
        accessToken = githubToken
        parent(rootProject.tasks.named("publishGithub"))
    }
}
