## Restricted Crafting Mod

![screenshot.png](screenshot.png)

A server-side mod that restricts **crafting** based on LuckPerms permissions.
There are two ways to prevert crafting something.

**By recipe**: deny `restricted-crafting.<recipe_id>`. 
For example, to stop players crafting iron ingots from nuggets, set `restricted-crafting.minecraft:iron_ingot_from_nuggets` to `false`. Other recipes for the same item stay craftable.

**By output item**: deny `restricted-crafting.item.<item_id>`.
To stop players crafting an iron ingot by any recipe at all, set `restricted-crafting.item.minecraft:iron_ingot` to `false`.

You can get all recipes by running `/restrictedcrafting dump` to list them, or find recipe ids in [misode/mcmeta](https://github.com/misode/mcmeta/tree/data-json/data/minecraft/recipe).

Some recipes (firework rocket, map cloning, book cloning, armor dyeing, banner duplicating, ...) build their result at craft time and they have no `restricted-crafting.item` node.
Restrict those by recipe id.

### Crafter (auto-crafter)

Deny `restricted-crafting.crafter.<recipe_id>` or `restricted-crafting.crafter.item.<item_id>` in the `default` group to stop the Crafter making it.

[CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/restricted-crafting), [Modrinth](https://modrinth.com/mod/restricted-crafting)

[Discord](https://discord.gg/UY4nhvUzaK)

### Dependencies

This mod requires:
- `Fabric API` [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api), [Modrinth](https://modrinth.com/mod/fabric-api) (Fabric only)
- `LuckPerms` [LuckPerms](https://luckperms.net/), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/luckperms), [Modrinth](https://modrinth.com/mod/luckperms)