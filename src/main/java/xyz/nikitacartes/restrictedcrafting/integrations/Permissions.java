package xyz.nikitacartes.restrictedcrafting.integrations;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}

import xyz.nikitacartes.restrictedcrafting.RestrictedCrafting;

import java.util.function.Predicate;

/**
 * Permission checks resolved straight through the LuckPerms API, so the same code path works on
 * Fabric and NeoForge (LuckPerms ships both). No fabric-permissions-api dependency needed.
 */
public final class Permissions {
    private Permissions() {}

    /**
     * @param def value used when LuckPerms is absent or the node is UNDEFINED (fail-open by design:
     *            a recipe is only restricted once an admin explicitly denies its node).
     */
    public static boolean check(ServerPlayer player, String node, boolean def) {
        LuckPerms luckPerms = RestrictedCrafting.luckPerms;
        if (luckPerms == null) {
            return def;
        }
        Tristate state = luckPerms.getPlayerAdapter(ServerPlayer.class)
                .getPermissionData(player)
                .checkPermission(node);
        return state == Tristate.UNDEFINED ? def : state.asBoolean();
    }

    /** Command gate by vanilla operator level (the {@code hasPermission(int)} API moved in 1.21.11). */
    public static Predicate<CommandSourceStack> requireLevel(int level) {
        //? if >=1.21.11 {
        return source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
        //?} else {
        /*return source -> source.hasPermission(level);
        *///?}
    }
}
