package xyz.nikitacartes.restrictedcrafting;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collection;

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

    public static Collection<RecipeEntry<?>> removeRestrictedRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        Collection<RecipeEntry<?>> restrictedRecipes = new ArrayList<>();
        Collection<RecipeEntry<?>> newRecipes = new ArrayList<>(recipes);
        newRecipes.removeIf(recipe -> {
            if (Permissions.check(player.getCommandSource(), "restricted-crafting." + recipe.id().toString(), true)) {
                return false;
            } else {
                restrictedRecipes.add(recipe);
                return true;
            }
        });
        player.lockRecipes(restrictedRecipes);

        return newRecipes;
    }
}