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
 * The real crafting gate. {@code RecipeCraftingHolder.setRecipeUsed(player, recipe)} (Yarn
 * {@code shouldCraftRecipe}) is what {@code CraftingMenu.slotChangedCraftingGrid} consults before
 * populating the result slot, for both the crafting table (3x3) and the inventory grid (2x2).
 * Forcing it to {@code false} leaves the output slot empty, so a player without permission cannot
 * craft the recipe regardless of the {@code doLimitedCrafting} gamerule. The permission check runs
 * before {@code original.call}, so the gamerule and {@code isSpecial} checks never run for a
 * disallowed recipe and special recipes are gated too. A cancelling {@code @Inject} would work as
 * well, but {@code @WrapMethod} nests with other mods that wrap the same method.
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
