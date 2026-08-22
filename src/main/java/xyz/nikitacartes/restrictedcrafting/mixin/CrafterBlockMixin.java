package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.isCrafterRestricted;
import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.recipeId;

/**
 * The auto-crafter has no player, so it follows a global policy read from the LuckPerms default
 * group ({@link xyz.nikitacartes.restrictedcrafting.RestrictedCrafting#isCrafterRestricted}).
 * {@code getPotentialResults} is the single point where the crafter resolves the recipe to make;
 * returning empty makes it refuse a restricted recipe. Matched by name (only overload), so no
 * version-specific descriptor is needed despite the Level/ServerLevel param change at 1.21.2.
 */
@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {

    @ModifyReturnValue(method = "getPotentialResults", at = @At("RETURN"))
    private static Optional<RecipeHolder<CraftingRecipe>> rc$blockRestricted(Optional<RecipeHolder<CraftingRecipe>> original) {
        if (original.isPresent() && isCrafterRestricted(recipeId(original.get()))) {
            return Optional.empty();
        }
        return original;
    }
}
