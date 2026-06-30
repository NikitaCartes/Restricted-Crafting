package xyz.nikitacartes.restrictedcrafting.mixin;

// Pre-1.21.2 only: before the recipe-display rework, the full recipe registry was pushed to clients
// via ClientboundUpdateRecipesPacket from PlayerList (on join and on datapack reload). Filtering it
// per player removes restricted recipes from the client entirely. On 1.21.2+ this is unnecessary
// (the registry packet no longer carries the recipe list, ServerRecipeBookMixin handles the book),
// so this mixin compiles to an empty no-op there.
//? if <1.21.2 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.removeRestrictedRecipes;
*///?}
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    //? if <1.21.2 {
    /*@ModifyExpressionValue(
            method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getOrderedRecipes()Ljava/util/Collection;"))
    private Collection<RecipeHolder<?>> rc$filterJoin(Collection<RecipeHolder<?>> original, @Local(argsOnly = true) ServerPlayer player) {
        return removeRestrictedRecipes(new ArrayList<>(original), player);
    }

    @WrapOperation(
            method = "reloadResources()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void rc$filterReload(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original, @Local ServerPlayer player) {
        if (packet instanceof ClientboundUpdateRecipesPacket recipesPacket) {
            original.call(instance, new ClientboundUpdateRecipesPacket(removeRestrictedRecipes(new ArrayList<>(recipesPacket.getRecipes()), player)));
        } else {
            original.call(instance, packet);
        }
    }
    *///?}
}
