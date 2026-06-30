plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
}
stonecutter active "1.21.11-fabric"

stonecutter parameters {
    // Loader flag constants: `//? if fabric { ... }` / `//? if neoforge { ... }`.
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")
}

stonecutter.tasks {
    // Sort published artifacts by version when running the aggregated publishMods.
    order("publishMods")
}
