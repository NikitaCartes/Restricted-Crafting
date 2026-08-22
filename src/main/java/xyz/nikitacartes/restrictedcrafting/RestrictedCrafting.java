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

    /** Base permission node. The crafting-table/inventory gate checks {@code <PERM>.<recipe-id>}. */
    public static final String PERM = "restricted-crafting";
    /** The auto-crafter (block) policy checks {@code <PERM_CRAFTER>.<recipe-id>} against the default group. */
    public static final String PERM_CRAFTER = "restricted-crafting.crafter";
    /** Denies every crafting recipe that outputs an item: {@code <PERM_ITEM>.<item-id>}. */
    public static final String PERM_ITEM = "restricted-crafting.item";
    /** The auto-crafter equivalent of {@link #PERM_ITEM}. */
    public static final String PERM_CRAFTER_ITEM = "restricted-crafting.crafter.item";

    public static LuckPerms luckPerms;
    public static Group defaultGroup;

    /**
     * Every node that must be allowed for one recipe to be craftable: the recipe's own node first,
     * then one per output item. Built once per datapack load, so the hot path never concatenates a
     * node string and an auto-crafter craft costs one map lookup plus one LuckPerms cache lookup.
     *
     * @param items output item ids, empty for a special recipe whose result is only known at craft time
     */
    public record RecipeNodes(List<String> player, List<String> crafter, List<String> items) {}

    /** Recipe id to its nodes, for every CRAFTING recipe. Replaced wholesale, hence volatile. */
    public static volatile Map<String, RecipeNodes> recipeNodes = new HashMap<>();

    // ---- shared init, called from each loader's events -------------------------------------------

    private static void onServerStarted(MinecraftServer server) {
        luckPerms = LuckPermsProvider.get();

        defaultGroup = luckPerms.getGroupManager().getGroup("default");
        if (defaultGroup == null) {
            LOGGER.warn("[RestrictedCrafting] No LuckPerms 'default' group found - no crafting restrictions will apply.");
            return;
        }

        registerNodes(server);
    }

    /** Re-run on every datapack reload so recipes added by datapacks get nodes and stay enforceable. */
    private static void onReload(MinecraftServer server) {
        if (defaultGroup == null) {
            return;
        }
        registerNodes(server);
    }

    /**
     * Register a permission node for every CRAFTING recipe and for every item those recipes output,
     * so admins see them in the LuckPerms editor. Non-crafting recipes (smelting, stonecutting,
     * smithing, ...) are intentionally skipped: this mod only enforces crafting, so advertising
     * nodes it cannot enforce would be misleading.
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

    /**
     * Item ids this recipe can produce. A recipe with a variable result (suspicious stew) reports
     * every variant. A special recipe reports nothing, because {@code CustomRecipe} declares no
     * display: its output is known only once the grid is filled, so it stays restricted by recipe id.
     */
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
        Collection<RecipeHolder<?>> restricted = new ArrayList<>();
        Collection<RecipeHolder<?>> allowed = new ArrayList<>(recipes);
        allowed.removeIf(recipe -> {
            if (allowedFor(player, recipeId(recipe))) {
                return false;
            } else {
                restricted.add(recipe);
                return true;
            }
        });
        player.resetRecipes(restricted);
        return allowed;
    }

    /** Whether this player may craft this recipe: its own node and every output-item node must allow it. */
    public static boolean allowedFor(ServerPlayer player, String id) {
        for (String node : nodesFor(id).player()) {
            if (!Permissions.check(player, node, true)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the auto-crafter must refuse this recipe, asked of the default group on every craft.
     * {@code checkPermission} resolves inheritance and wildcards and is itself a map lookup into a
     * cache that LuckPerms invalidates on any change, so caching the answer here would only add a
     * second copy that goes stale when a parent group or a network sync changes the outcome. A
     * recipe is denied when any of its nodes is explicitly FALSE.
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

    /** Falls back to the recipe node alone for a recipe that arrived after the last registration pass. */
    private static RecipeNodes nodesFor(String id) {
        RecipeNodes nodes = recipeNodes.get(id);
        return nodes != null
                ? nodes
                : new RecipeNodes(List.of(PERM + "." + id), List.of(PERM_CRAFTER + "." + id), List.of());
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
