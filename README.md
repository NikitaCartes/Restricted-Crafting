## Restricted Crafting Mod

This mod allows you to restrict crafting of items based on permissions.  
You can restrict crafting of any item by adding a permission node `restricted-crafting.<minecraft item>`.  
For example, to restrict crafting of iron ingots, add `restricted-crafting.minecraft:iron_ingot` with value `false`.

To restrict crafting for Crafter, add a permission node `restricted-crafting.crafter.<minecraft item>` in `default` LuckPerms group.

### Dependencies

This mod requires:
- `Fabric API` [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api), [Modrinth](https://modrinth.com/mod/fabric-api)
- `LuckPerms` [LuckPerms](https://luckperms.net/), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/luckperms), [Modrinth](https://modrinth.com/mod/luckperms)