package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
//? if <1.21.2
/*import net.minecraft.world.level.Level;*/
import org.spongepowered.asm.mixin.Mixin;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.allowedFor;
import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.recipeId;

/**
 * The real gate: deny here and the result slot stays empty (3x3, 2x2, special recipes).
 * WrapMethod nests with other mods wrapping the same method.
 */
@Mixin(RecipeCraftingHolder.class)
public interface CraftingScreenHandlerMixin {

    //? if <1.21.2 {
    /*@WrapMethod(method = "setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z")
    private boolean rc$gate(Level level, ServerPlayer player, RecipeHolder<?> recipe, Operation<Boolean> original) {
        return allowedFor(player, recipeId(recipe)) && original.call(level, player, recipe);
    }
    *///?} else {
    @WrapMethod(method = "setRecipeUsed(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z")
    private boolean rc$gate(ServerPlayer player, RecipeHolder<?> recipe, Operation<Boolean> original) {
        return allowedFor(player, recipeId(recipe)) && original.call(player, recipe);
    }
    //?}
}
