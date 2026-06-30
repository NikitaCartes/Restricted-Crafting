## Restricted Crafting Mod

![screenshot.png](screenshot.png)

A server-side mod that restricts **crafting** based on LuckPerms permissions, on **Fabric and NeoForge**.
Restrict any crafting recipe by denying the permission node `restricted-crafting.<recipe_id>` — e.g. to
stop players crafting iron ingots from nuggets, set `restricted-crafting.minecraft:iron_ingot_from_nuggets`
to `false`.

At server startup (and on every datapack `/reload`) the mod registers a permission node for every crafting
recipe, so they all show up in the LuckPerms editor. You can also find recipe ids in
[misode/mcmeta](https://github.com/misode/mcmeta/tree/data-json/data/minecraft/recipe).

Permissions are **fail-open**: a recipe is craftable unless its node is explicitly denied. Only crafting-table,
inventory-grid and auto-crafter recipes are enforced — smelting, stonecutting, smithing, etc. are out of scope
and intentionally get no nodes.

### Crafter (auto-crafter)

The crafter block has no owner, so it follows the `default` LuckPerms group. Deny
`restricted-crafting.crafter.<recipe_id>` in the `default` group to stop the crafter making it.
Wildcards and group inheritance are resolved, so `restricted-crafting.crafter.minecraft:*` works.

### Commands

- `/restrictedcrafting dump [file]` — write every managed permission node and its current default-group
  state to a file in the server directory (default `restricted-crafting-permissions.txt`). Requires op level 4.

### Supported versions

- **Fabric:** 1.21 – 1.21.11, 26.1, 26.2
- **NeoForge:** 1.21 – 1.21.11, 26.1

This is a [Stonecutter](https://stonecutter.kikugie.dev/) project: one Mojang-mapped source tree, one node per
Minecraft compatibility bucket. `./gradlew build` builds every node.

[CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/restricted-crafting),
[Modrinth](https://modrinth.com/mod/restricted-crafting), [Discord](https://discord.gg/UY4nhvUzaK)

### Dependencies

- **LuckPerms** — [website](https://luckperms.net/), [Modrinth](https://modrinth.com/mod/luckperms) (required, both loaders)
- **Fabric API** — [Modrinth](https://modrinth.com/mod/fabric-api) (Fabric only)
