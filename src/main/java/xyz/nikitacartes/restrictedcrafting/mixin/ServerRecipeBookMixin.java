package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.function.Consumer;

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

    @WrapOperation(method = "sendInitRecipesPacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerRecipeBook$DisplayCollector;displaysForRecipe(Lnet/minecraft/registry/RegistryKey;Ljava/util/function/Consumer;)V")
    )
    private void doNotAddRestrictedRecipe(ServerRecipeBook.DisplayCollector instance, RegistryKey<Recipe<?>> recipeRegistryKey, Consumer<RecipeDisplayEntry> recipeDisplayEntryConsumer, Operation<Void> original, @Local(argsOnly = true) ServerPlayerEntity player) {
        if (!Permissions.check(player.getCommandSource(), "restricted-crafting." + recipeRegistryKey.getValue(), true)) {
            return;
        }
        original.call(instance, recipeRegistryKey, recipeDisplayEntryConsumer);
    }

}
