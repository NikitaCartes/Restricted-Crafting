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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
//? if >=1.21.2 {
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
//?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.nikitacartes.restrictedcrafting.command.DumpPermissionsCommand;
import xyz.nikitacartes.restrictedcrafting.integrations.Permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//? if fabric {
public class RestrictedCrafting implements ModInitializer {
//?} else {
/*@Mod("restrictedcrafting")
public class RestrictedCrafting {
*///?}

    public static final Logger LOGGER = LoggerFactory.getLogger("RestrictedCrafting");

    /** Player gate: {@code <PERM>.<recipe-id>}. */
    public static final String PERM = "restricted-crafting";
    /** Crafter gate, checked against the default group. */
    public static final String PERM_CRAFTER = "restricted-crafting.crafter";
    /** Denies every recipe outputting an item. */
    public static final String PERM_ITEM = "restricted-crafting.item";
    /** Crafter equivalent of {@link #PERM_ITEM}. */
    public static final String PERM_CRAFTER_ITEM = "restricted-crafting.crafter.item";

    public static LuckPerms luckPerms;
    public static Group defaultGroup;

    /**
     * Nodes that must all allow a recipe: own node first, then one per output item.
     * Built once per datapack load so the hot path never concatenates strings.
     *
     * @param items output item ids, empty for special recipes (result known only at craft time)
     */
    public record RecipeNodes(List<String> player, List<String> crafter, List<String> items) {}

    /** Recipe id -> nodes, rebuilt per datapack load (volatile: replaced wholesale). */
    public static volatile Map<String, RecipeNodes> recipeNodes = new HashMap<>();

    private static void onServerStarted(MinecraftServer server) {
        luckPerms = LuckPermsProvider.get();

        defaultGroup = luckPerms.getGroupManager().getGroup("default");
        if (defaultGroup == null) {
            LOGGER.warn("[RestrictedCrafting] No LuckPerms 'default' group found - no crafting restrictions will apply.");
            return;
        }

        registerNodes(server);
    }

    /** Re-run per datapack reload so datapack recipes stay enforceable. */
    private static void onReload(MinecraftServer server) {
        if (defaultGroup == null) {
            return;
        }
        registerNodes(server);
        // Evict already-granted denials (the filter only gates new grants; resetting there double-sends).
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            List<RecipeHolder<?>> denied = new ArrayList<>();
            for (RecipeHolder<?> recipe : server.getRecipeManager().getRecipes()) {
                if (recipe.value() instanceof CraftingRecipe && !allowedFor(player, recipeId(recipe))) {
                    denied.add(recipe);
                }
            }
            if (!denied.isEmpty()) {
                player.resetRecipes(denied);
            }
        }
    }

    /**
     * Advertise one node per CRAFTING recipe + per output item. Skips smelting/stonecutting/etc:
     * only crafting is enforced, so other nodes would mislead.
     */
    public static void registerNodes(MinecraftServer server) {
        if (defaultGroup == null) {
            return;
        }
        CachedPermissionData permissionData = defaultGroup.getCachedData().getPermissionData();
        permissionData.checkPermission(PERM);
        permissionData.checkPermission(PERM_CRAFTER);
        permissionData.checkPermission(PERM_ITEM);
        permissionData.checkPermission(PERM_CRAFTER_ITEM);

        Map<String, RecipeNodes> byRecipe = new HashMap<>();
        for (RecipeHolder<?> recipe : server.getRecipeManager().getRecipes()) {
            if (!(recipe.value() instanceof CraftingRecipe)) {
                continue;
            }
            String id = recipeId(recipe);
            List<String> items = List.copyOf(outputItems(server, recipe));
            List<String> player = new ArrayList<>(items.size() + 1);
            List<String> crafter = new ArrayList<>(items.size() + 1);
            player.add(PERM + "." + id);
            crafter.add(PERM_CRAFTER + "." + id);
            for (String item : items) {
                player.add(PERM_ITEM + "." + item);
                crafter.add(PERM_CRAFTER_ITEM + "." + item);
            }
            player.forEach(permissionData::checkPermission);
            crafter.forEach(permissionData::checkPermission);
            byRecipe.put(id, new RecipeNodes(List.copyOf(player), List.copyOf(crafter), items));
        }
        recipeNodes = byRecipe;
    }

    /** Item ids a recipe can output (every variant); empty for special recipes, gated by id. */
    private static Set<String> outputItems(MinecraftServer server, RecipeHolder<?> recipe) {
        Set<String> items = new LinkedHashSet<>();
        //? if <1.21.2 {
        /*addItem(items, recipe.value().getResultItem(server.registryAccess()));
        *///?} else {
        ContextMap context = SlotDisplayContext.fromLevel(server.overworld());
        for (RecipeDisplay display : recipe.value().display()) {
            for (ItemStack stack : display.result().resolveForStacks(context)) {
                addItem(items, stack);
            }
        }
        //?}
        return items;
    }

    private static void addItem(Set<String> items, ItemStack stack) {
        if (!stack.isEmpty()) {
            items.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        }
    }

    public static Collection<RecipeHolder<?>> removeRestrictedRecipes(Collection<RecipeHolder<?>> recipes, ServerPlayer player) {
        Collection<RecipeHolder<?>> allowed = new ArrayList<>(recipes);
        allowed.removeIf(recipe -> !allowedFor(player, recipeId(recipe)));
        return allowed;
    }

    /** Player may craft id only if every node allows it. */
    public static boolean allowedFor(ServerPlayer player, String id) {
        for (String node : nodesFor(id).player()) {
            if (!Permissions.check(player, node, true)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Crafter refusal per the default group. Live LuckPerms lookup (resolves inheritance/wildcards);
     * no local copy to go stale. Denied if any node is explicitly FALSE.
     */
    public static boolean isCrafterRestricted(String id) {
        Group group = defaultGroup;
        if (group == null) {
            return false;
        }
        CachedPermissionData permissionData = group.getCachedData().getPermissionData();
        for (String node : nodesFor(id).crafter()) {
            if (permissionData.checkPermission(node) == Tristate.FALSE) {
                return true;
            }
        }
        return false;
    }

    /** Fallback for recipes that arrived after the last registration pass. */
    private static RecipeNodes nodesFor(String id) {
        RecipeNodes nodes = recipeNodes.get(id);
        return nodes != null
                ? nodes
                : new RecipeNodes(List.of(PERM + "." + id), List.of(PERM_CRAFTER + "." + id), List.of());
    }

    public static String recipeId(RecipeHolder<?> recipe) {
        //? if <1.21.2 {
        /*return recipe.id().toString();
        *///?} elif <1.21.11 {
        /*return recipe.id().location().toString();
        *///?} else {
        return recipe.id().identifier().toString();
        //?}
    }

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
