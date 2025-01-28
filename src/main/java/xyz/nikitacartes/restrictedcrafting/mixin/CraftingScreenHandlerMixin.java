package xyz.nikitacartes.restrictedcrafting.mixin;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.stripRegistryKey;

@Mixin(RecipeUnlocker.class)
public interface CraftingScreenHandlerMixin {

    @Inject(method = "shouldCraftRecipe(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/RecipeEntry;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void doNotSetOutput(ServerPlayerEntity player, RecipeEntry<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!Permissions.check(player.getCommandSource(), "restricted-crafting." + stripRegistryKey(recipe), true)) {
            cir.setReturnValue(false);
        }
    }
}