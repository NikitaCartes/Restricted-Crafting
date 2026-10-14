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
 * Crafter refusal via the default group: empty result = no craft. Matched by name, so the 1.21.2
 * Level/ServerLevel param change needs no branches.
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
