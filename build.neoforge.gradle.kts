plugins {
    id("java")
    id("net.neoforged.moddev") version "2.0.141"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

// Tag this node's loader and version so [neoforge."x"] keys resolve via bare property("...").
stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

// NeoForge mod ids may not contain '-'; the Fabric id stays "restricted-crafting".
val neoModId = "restrictedcrafting"

base.archivesName = "${property("mod_id")}-neoforge-mc${property("minecraft_version")}"
version = property("mod_version").toString()

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
}

neoForge {
    version = property("neoforge_version").toString()

    runs {
        create("server") {
            server()
            gameDirectory.set(file("run"))
        }
    }
    mods {
        create(neoModId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

// jar-in-jar for runtime libs.
fun DependencyHandlerScope.implementAndJarJar(notation: String, version: String) {
    implementation(notation)
    jarJar(notation) {
        version { strictly("[$version,)"); prefer(version) }
    }
}

dependencies {
    implementAndJarJar("io.github.llamalad7:mixinextras-neoforge:${property("mixinextras_version")}", property("mixinextras_version").toString())

    // LuckPerms API (provided at runtime by the LuckPerms mod)
    compileOnly("net.luckperms:api:${property("luckperms_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

val modExpansions = mapOf(
    "version" to project.version.toString(),
    "supported_minecraft_version" to property("supported_minecraft_version").toString(),
    "neoforge_version" to property("neoforge_version").toString(),
    "mod_id" to neoModId,
    "mod_name" to property("mod_name").toString()
)

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.properties(modExpansions)
    filesMatching("META-INF/neoforge.mods.toml") { expand(modExpansions) }

    // NeoForge runs on official Mojang names, so no mixin refmap.
    filesMatching("restricted-crafting.mixins.json") {
        filter { it.replace("\"refmap\": \"\${refmap}\",", "") }
    }
    // Fabric-only metadata is irrelevant on NeoForge.
    exclude("fabric.mod.json")
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

tasks.jar {
    from("LICENSE")
}

tasks.register<Copy>("collectJars") {
    group = "build"
    from(tasks.jar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
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
    modLoaders.add("neoforge")

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
    }
    // Uploads this node's jar into the single release created by the root publishGithub task.
    github {
        accessToken = githubToken
        parent(rootProject.tasks.named("publishGithub"))
    }
}
