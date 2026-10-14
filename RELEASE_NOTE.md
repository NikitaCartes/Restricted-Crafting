**Minecraft 1.21 - 1.21.11, 26.1 - 26.3** · Fabric and NeoForge · requires LuckPerms

### Restrict by output item

A new permission axis restricts an item rather than one path to it:

- `restricted-crafting.item.<item_id>` blocks every crafting recipe that makes the item, for a player.
- `restricted-crafting.crafter.item.<item_id>` does the same for the auto-crafter, read from the `default` group.

Special recipes (firework rocket, map cloning, armor dyeing, ...) build their result at craft time and declare no output item, so restrict those by recipe id.

### Everything else

1) NeoForge support added
2) Only **crafting** recipes now get permission nodes - smelting/stonecutting/smithing/etc. are no longer suggested, since the mod can only check crafting
3) Permission nodes are re-registered on datapack `/reload`, so recipes added by datapacks become restrictable without a restart
4) New command `/restrictedcrafting dump` writes every managed permission node and its default-group state to `restricted-crafting/permissions.txt` (op level 4)
5) Permission checks go straight through the LuckPerms API on both loaders (the `fabric-permissions-api` dependency is gone)
6) New permission axis by output item: `restricted-crafting.item.<item_id>` and `restricted-crafting.crafter.item.<item_id>` restrict every recipe that makes an item, including recipes added later by a datapack
7) `/restrictedcrafting dump` now also lists every crafting recipe under the item it outputs
8) Hardening: dump writes a fixed path, denied recipes are evicted from the book on `/reload`, book-hiding no longer fails the boot on new versions
