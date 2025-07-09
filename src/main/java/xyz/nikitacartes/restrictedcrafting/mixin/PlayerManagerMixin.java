package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.removeRestrictedRecipes;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @ModifyExpressionValue(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;sortedValues()Ljava/util/Collection;"))
    private Collection<RecipeEntry<?>> addRestrictions(Collection<RecipeEntry<?>> original, @Local(argsOnly = true) ServerPlayerEntity player) {
        return removeRestrictedRecipes(new ArrayList<>(original),
                player);
    }

    @WrapOperation(method = "onDataPacksReloaded()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void addRestrictions(ServerPlayNetworkHandler instance, Packet packet, Operation<Void> original, @Local ServerPlayerEntity serverPlayerEntity) {
        original.call(instance,
                new SynchronizeRecipesS2CPacket(
                        removeRestrictedRecipes((
                                (SynchronizeRecipesS2CPacket) packet).getRecipes(),
                                serverPlayerEntity)));
    }
}