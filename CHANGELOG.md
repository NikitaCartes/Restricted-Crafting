### 1.0.0
##### Minecraft 1.21.x, 26.1-26.3, Fabric and NeoForge

1) NeoForge support added
2) Only **crafting** recipes now get permission nodes - smelting/stonecutting/smithing/etc. are no longer suggested, since the mod can only check crafting
3) Permission nodes are re-registered on datapack `/reload`, so recipes added by datapacks become restrictable without a restart
4) New command `/restrictedcrafting dump` writes every managed permission node and its default-group state to `restricted-crafting/permissions.txt` (op level 4)
5) New permission axis by output item: `restricted-crafting.item.<item_id>` and `restricted-crafting.crafter.item.<item_id>` restrict every recipe that makes an item, including recipes added later by a datapack

### 0.7.0
##### Minecraft 1.21-1.21.1, 1.21.2-1.21.7

1) Register permissions for all recipes at server startup

### 0.6.0
##### Minecraft 1.21-1.21.1, 1.21.2-1.21.5

1) Optimize crafter recipe check

### 0.5.0
##### Minecraft 1.21.1

1) Do not show crafting recipe if player does not have permission to craft it instead of cancelling crafting
2) Do not shoe restricted crafting recipes in recipe book
3) Do not show restricted crafting recipes in crafter

### 0.4.0
##### Minecraft 1.21.1

1) Fix compatibility issue with 1.21.1

### 0.3.0
##### Minecraft 1.21.1

1) Add support for server-side translation

### 0.2.0
##### Minecraft 1.21.1

1) Add restrictions for Crafter: `restricted-crafting.crafter.<minecraft item>` in Default LuckPerms Group, e.g. `restricted-crafting.crafter.minecraft:iron_ingot`

### 0.1.0
##### Minecraft 1.21.1

1) Restrict crafting based on permissions `restricted-crafting.<minecraft item>`, e.g. `restricted-crafting.minecraft:iron_ingot`
