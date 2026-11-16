package xyz.nikitacartes.restrictedcrafting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import xyz.nikitacartes.restrictedcrafting.RestrictedCrafting;
import xyz.nikitacartes.restrictedcrafting.integrations.Permissions;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else {
/*import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** /restrictedcrafting dump: node table + recipes-by-item table, into restricted-crafting/permissions.txt. */
public final class DumpPermissionsCommand {
    private DumpPermissionsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("restrictedcrafting")
                .requires(Permissions.requireLevel(4))
                .then(Commands.literal("dump")
                        .executes(DumpPermissionsCommand::dump)));
    }

    private static int dump(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (RestrictedCrafting.defaultGroup == null) {
            source.sendFailure(Component.literal("[RestrictedCrafting] LuckPerms 'default' group not available."));
            return 0;
        }

        CachedPermissionData permissionData = RestrictedCrafting.defaultGroup.getCachedData().getPermissionData();
        Map<String, RestrictedCrafting.RecipeNodes> byRecipe = RestrictedCrafting.recipeNodes;

        Set<String> nodes = new TreeSet<>();
        Map<String, Set<String>> recipesByItem = new TreeMap<>();
        byRecipe.forEach((id, recipe) -> {
            nodes.addAll(recipe.player());
            nodes.addAll(recipe.crafter());
            for (String item : recipe.items()) {
                recipesByItem.computeIfAbsent(item, key -> new TreeSet<>()).add(id);
            }
        });

        List<String> lines = new ArrayList<>();
        lines.add("# RestrictedCrafting permission nodes");
        lines.add("# columns: <node>\t<default state: true|false|undefined>");
        for (String node : nodes) {
            lines.add(node + "\t" + state(permissionData, node));
        }

        lines.add("");
        lines.add("# Crafting recipes by output item. Set false for every recipe listed for an item, or set false for");
        lines.add("# " + RestrictedCrafting.PERM_ITEM + ".<item_id> to cover all of them at once.");
        lines.add("# Special recipes (firework rocket, map cloning, ...) build their result at craft");
        lines.add("# time and are absent here; restrict those by recipe id above.");
        lines.add("# columns: <item_id>\t<recipe_id>");
        recipesByItem.forEach((item, ids) -> ids.forEach(id -> lines.add(item + "\t" + id)));

        //? if fabric {
        Path path = FabricLoader.getInstance().getGameDir().resolve("restricted-crafting/permissions.txt");
        //?} else {
        /*Path path = FMLPaths.GAMEDIR.get().resolve("restricted-crafting/permissions.txt");
        *///?}
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines);
        } catch (IOException e) {
            source.sendFailure(Component.literal("[RestrictedCrafting] Failed to write " + path + ": " + e.getMessage()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("[RestrictedCrafting] Dumped " + nodes.size() + " permission nodes for " + byRecipe.size() + " crafting recipes to " + path), false);
        return byRecipe.size();
    }

    private static String state(CachedPermissionData data, String node) {
        Tristate tristate = data.checkPermission(node);
        return tristate == Tristate.TRUE ? "true" : tristate == Tristate.FALSE ? "false" : "undefined";
    }
}
