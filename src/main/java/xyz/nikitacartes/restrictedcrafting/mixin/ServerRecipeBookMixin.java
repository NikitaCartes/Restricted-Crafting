package xyz.nikitacartes.restrictedcrafting.mixin;

import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.removeRestrictedRecipes;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {

    @ModifyVariable(method = "unlockRecipes(Ljava/util/Collection;Lnet/minecraft/server/network/ServerPlayerEntity;)I",
            at = @At(value = "HEAD"),
            ordinal = 0,
            argsOnly = true)
    private Collection<RecipeEntry<?>> doNotReturnRestrictedItem(Collection<RecipeEntry<?>> recipes, Collection<RecipeEntry<?>> ignored, ServerPlayerEntity player) {
        return removeRestrictedRecipes(recipes, player);
    }
}
