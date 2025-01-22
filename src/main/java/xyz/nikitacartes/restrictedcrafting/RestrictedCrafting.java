package xyz.nikitacartes.restrictedcrafting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.minecraft.server.MinecraftServer;

public class RestrictedCrafting implements ModInitializer {
    public static Group defaultGroup;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);
    }

    private void onStartServer(MinecraftServer server) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        defaultGroup = luckPerms.getGroupManager().getGroup("default");
        if (defaultGroup == null) {
            return;
        }

        CachedPermissionData permissionData = defaultGroup.getCachedData().getPermissionData();
        permissionData.checkPermission("restricted-crafting");
        permissionData.checkPermission("restricted-crafting.crafter");
    }
}