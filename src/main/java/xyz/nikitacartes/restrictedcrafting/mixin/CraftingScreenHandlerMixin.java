package xyz.nikitacartes.restrictedcrafting.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
//? if <1.21.2
/*import net.minecraft.world.level.Level;*/
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xyz.nikitacartes.restrictedcrafting.integrations.Permissions;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.PERM;
import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.recipeId;

/**
 * The real crafting gate. {@code RecipeCraftingHolder.setRecipeUsed(player, recipe)} (Yarn
 * {@code shouldCraftRecipe}) is what {@code CraftingMenu.slotChangedCraftingGrid} consults before
 * populating the result slot, for both the crafting table (3x3) and the inventory grid (2x2).
 * Forcing it to {@code false} leaves the output slot empty, so a player without permission cannot
 * craft the recipe regardless of the {@code doLimitedCrafting} gamerule.
 */
@Mixin(RecipeCraftingHolder.class)
public interface CraftingScreenHandlerMixin {

    //? if <1.21.2 {
    /*@Inject(method = "setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z", at = @At("HEAD"), cancellable = true)
    private void rc$gate(Level level, ServerPlayer player, RecipeHolder<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!Permissions.check(player, PERM + "." + recipeId(recipe), true)) {
            cir.setReturnValue(false);
        }
    }
    *///?} else {
    @Inject(method = "setRecipeUsed(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z", at = @At("HEAD"), cancellable = true)
    private void rc$gate(ServerPlayer player, RecipeHolder<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!Permissions.check(player, PERM + "." + recipeId(recipe), true)) {
            cir.setReturnValue(false);
        }
    }
    //?}
}
