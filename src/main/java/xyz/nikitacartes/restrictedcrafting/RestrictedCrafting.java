package xyz.nikitacartes.restrictedcrafting;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
//?} else {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
*///?}

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.nikitacartes.restrictedcrafting.command.DumpPermissionsCommand;
import xyz.nikitacartes.restrictedcrafting.integrations.Permissions;
import xyz.nikitacartes.restrictedcrafting.listener.LuckPermsListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//? if fabric {
public class RestrictedCrafting implements ModInitializer {
//?} else {
/*@Mod("restrictedcrafting")
public class RestrictedCrafting {
*///?}

    public static final Logger LOGGER = LoggerFactory.getLogger("RestrictedCrafting");

    /** Base permission node. The crafting-table/inventory gate checks {@code <PERM>.<recipe-id>}. */
    public static final String PERM = "restricted-crafting";
    /** The auto-crafter (block) policy checks {@code <PERM_CRAFTER>.<recipe-id>} against the default group. */
    public static final String PERM_CRAFTER = "restricted-crafting.crafter";

    public static LuckPerms luckPerms;
    public static Group defaultGroup;

    /** Recipe ids of every CRAFTING recipe; what we actually enforce. Replaced wholesale, hence volatile. */
    public static volatile Set<String> craftingRecipeIds = new HashSet<>();
    /** Recipe ids the auto-crafter must refuse, derived from the default group. Replaced wholesale, hence volatile. */
    public static volatile Set<String> restrictedRecipesForCrafter = new HashSet<>();

    // ---- shared init, called from each loader's events -------------------------------------------

    private static void onServerStarted(MinecraftServer server) {
        luckPerms = LuckPermsProvider.get();

        defaultGroup = luckPerms.getGroupManager().getGroup("default");
        if (defaultGroup == null) {
            LOGGER.warn("[RestrictedCrafting] No LuckPerms 'default' group found - no crafting restrictions will apply.");
            return;
        }

        registerNodes(server);
        new LuckPermsListener(luckPerms).registerListeners();
        updateCrafterRestriction();
    }

    /** Re-run on every datapack reload so recipes added by datapacks get nodes and stay enforceable. */
    private static void onReload(MinecraftServer server) {
        if (defaultGroup == null) {
            return;
        }
        registerNodes(server);
        updateCrafterRestriction();
    }

    /**
     * Register a permission node for every CRAFTING recipe so admins see them in the LuckPerms editor.
     * Non-crafting recipes (smelting, stonecutting, smithing, ...) are intentionally skipped: this mod
     * only enforces crafting, so advertising nodes it cannot enforce would be misleading.
     */
    public static void registerNodes(MinecraftServer server) {
        if (defaultGroup == null) {
            return;
        }
        CachedPermissionData permissionData = defaultGroup.getCachedData().getPermissionData();
        permissionData.checkPermission(PERM);
        permissionData.checkPermission(PERM_CRAFTER);

        HashSet<String> ids = new HashSet<>();
        for (RecipeHolder<?> recipe : server.getRecipeManager().getRecipes()) {
            if (!(recipe.value() instanceof CraftingRecipe)) {
                continue;
            }
            String id = recipeId(recipe);
            ids.add(id);
            permissionData.checkPermission(PERM + "." + id);
            permissionData.checkPermission(PERM_CRAFTER + "." + id);
        }
        craftingRecipeIds = ids;
    }

    public static Collection<RecipeHolder<?>> removeRestrictedRecipes(Collection<RecipeHolder<?>> recipes, ServerPlayer player) {
        Collection<RecipeHolder<?>> restricted = new ArrayList<>();
        Collection<RecipeHolder<?>> allowed = new ArrayList<>(recipes);
        allowed.removeIf(recipe -> {
            if (Permissions.check(player, PERM + "." + recipeId(recipe), true)) {
                return false;
            } else {
                restricted.add(recipe);
                return true;
            }
        });
        player.resetRecipes(restricted);
        return allowed;
    }

    /**
     * Recompute the auto-crafter deny set from the default group. Uses {@code checkPermission} (which
     * resolves inheritance and wildcards) rather than scanning literal nodes, so e.g.
     * {@code restricted-crafting.crafter.minecraft:*} works. A recipe is denied only when explicitly FALSE.
     */
    public static void updateCrafterRestriction() {
        if (defaultGroup == null) {
            return;
        }
        CachedPermissionData permissionData = defaultGroup.getCachedData().getPermissionData();
        HashSet<String> restricted = new HashSet<>();
        for (String id : craftingRecipeIds) {
            if (permissionData.checkPermission(PERM_CRAFTER + "." + id) == Tristate.FALSE) {
                restricted.add(id);
            }
        }
        restrictedRecipesForCrafter = restricted;
    }

    /** The recipe's namespaced id as the permission-node suffix. */
    public static String recipeId(RecipeHolder<?> recipe) {
        //? if <1.21.2 {
        /*return recipe.id().toString();
        *///?} elif <1.21.11 {
        /*return recipe.id().location().toString();
        *///?} else {
        return recipe.id().identifier().toString();
        //?}
    }

    // ---- loader entrypoints ----------------------------------------------------------------------

    //? if fabric {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(RestrictedCrafting::onServerStarted);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> onReload(server));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DumpPermissionsCommand.register(dispatcher));
    }
    //?} else {
    /*public RestrictedCrafting() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> onServerStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                onReload(event.getPlayerList().getServer());
            }
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> DumpPermissionsCommand.register(event.getDispatcher()));
    }
    *///?}
}
