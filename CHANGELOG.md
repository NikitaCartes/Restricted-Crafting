### 0.8.0
##### Minecraft 1.21 - 1.21.11, 26.1, 26.2 (Fabric) · 1.21 - 1.21.11, 26.1 (NeoForge)

1) Multi-version + multi-loader build via Stonecutter (Fabric and NeoForge from one Mojang-mapped source tree)
2) NeoForge support added (mod id `restrictedcrafting`)
3) Only **crafting** recipes now get permission nodes - smelting/stonecutting/smithing/etc. are no longer advertised, since the mod can only enforce crafting
4) Permission nodes are re-registered on datapack `/reload`, so recipes added by datapacks become restrictable without a restart
5) Crafter (auto-crafter) restrictions now resolve LuckPerms inheritance and wildcards (e.g. `restricted-crafting.crafter.minecraft:*`)
6) New command `/restrictedcrafting dump [file]` writes every managed permission node and its default-group state to a file (op level 4)
7) Permission checks go straight through the LuckPerms API on both loaders (the `fabric-permissions-api` dependency is gone)

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
