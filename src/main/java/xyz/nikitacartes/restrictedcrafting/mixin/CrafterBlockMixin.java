package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.luckperms.api.util.Tristate;
import net.minecraft.block.CrafterBlock;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.defaultGroup;

@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {

    @ModifyReturnValue(method = "getCraftingRecipe(Lnet/minecraft/world/World;Lnet/minecraft/recipe/input/CraftingRecipeInput;)Ljava/util/Optional;",
            at = @At(value = "RETURN"))
    private static Optional<RecipeEntry<CraftingRecipe>> doNotReturnRestrictedItem(Optional<RecipeEntry<CraftingRecipe>> original) {
        if (original.isPresent() && defaultGroup.getCachedData().getPermissionData().checkPermission("restricted-crafting.crafter." + original.get().id().toString()) == Tristate.FALSE) {
            return Optional.empty();
        }
        return original;
    }
}
