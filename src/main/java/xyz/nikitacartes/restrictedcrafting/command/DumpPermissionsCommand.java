package xyz.nikitacartes.restrictedcrafting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import xyz.nikitacartes.restrictedcrafting.RestrictedCrafting;
import xyz.nikitacartes.restrictedcrafting.integrations.Permissions;
import xyz.nikitacartes.restrictedcrafting.integrations.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code /restrictedcrafting dump [file]} writes every managed permission node and its resolved
 * default-group state to a file in the game directory. Useful for seeding a LuckPerms config or
 * auditing what the mod controls. Requires operator level 4.
 */
public final class DumpPermissionsCommand {
    private DumpPermissionsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("restrictedcrafting")
                .requires(Permissions.requireLevel(4))
                .then(Commands.literal("dump")
                        .executes(ctx -> dump(ctx, "restricted-crafting-permissions.txt"))
                        .then(Commands.argument("file", StringArgumentType.string())
                                .executes(ctx -> dump(ctx, StringArgumentType.getString(ctx, "file"))))));
    }

    private static int dump(CommandContext<CommandSourceStack> ctx, String fileName) {
        CommandSourceStack source = ctx.getSource();
        if (RestrictedCrafting.defaultGroup == null) {
            source.sendFailure(Component.literal("[RestrictedCrafting] LuckPerms 'default' group not available."));
            return 0;
        }

        CachedPermissionData permissionData = RestrictedCrafting.defaultGroup.getCachedData().getPermissionData();
        List<String> ids = new ArrayList<>(RestrictedCrafting.craftingRecipeIds);
        ids.sort(null);

        List<String> lines = new ArrayList<>();
        lines.add("# RestrictedCrafting permission nodes");
        lines.add("# columns: <node>\t<default-group state: allow|deny|unset>");
        lines.add(RestrictedCrafting.PERM + "\t" + state(permissionData, RestrictedCrafting.PERM));
        lines.add(RestrictedCrafting.PERM_CRAFTER + "\t" + state(permissionData, RestrictedCrafting.PERM_CRAFTER));
        for (String id : ids) {
            String craft = RestrictedCrafting.PERM + "." + id;
            String crafter = RestrictedCrafting.PERM_CRAFTER + "." + id;
            lines.add(craft + "\t" + state(permissionData, craft));
            lines.add(crafter + "\t" + state(permissionData, crafter));
        }

        Path path = Platform.gameDir().resolve(fileName).normalize();
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            source.sendFailure(Component.literal("[RestrictedCrafting] Failed to write " + path + ": " + e.getMessage()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("[RestrictedCrafting] Dumped " + ids.size() + " crafting recipe permissions to " + path), false);
        return ids.size();
    }

    private static String state(CachedPermissionData data, String node) {
        Tristate tristate = data.checkPermission(node);
        return tristate == Tristate.TRUE ? "allow" : tristate == Tristate.FALSE ? "deny" : "unset";
    }
}
