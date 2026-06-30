package xyz.nikitacartes.restrictedcrafting.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

//? if >=1.21.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

import java.util.function.Consumer;

import xyz.nikitacartes.restrictedcrafting.integrations.Permissions;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.PERM;
//?}

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.removeRestrictedRecipes;

/**
 * Hides restricted recipes from a player's recipe book. {@code addRecipes} (Yarn
 * {@code unlockRecipes}) is filtered on every version; on 1.21.2+ the per-recipe display callback
 * inside {@code sendInitialRecipeBook} is additionally suppressed so the initial book never lists a
 * restricted recipe. (Pre-1.21.2, the global recipe registry is filtered in PlayerListMixin instead.)
 */
@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {

    @ModifyVariable(method = "addRecipes(Ljava/util/Collection;Lnet/minecraft/server/level/ServerPlayer;)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Collection<RecipeHolder<?>> rc$filterUnlock(Collection<RecipeHolder<?>> recipes, Collection<RecipeHolder<?>> ignored, ServerPlayer player) {
        return removeRestrictedRecipes(recipes, player);
    }

    //? if >=1.21.2 {
    @WrapOperation(
            method = "sendInitialRecipeBook(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/ServerRecipeBook$DisplayResolver;displaysForRecipe(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Consumer;)V"))
    private void rc$filterDisplay(ServerRecipeBook.DisplayResolver instance, ResourceKey<Recipe<?>> recipeKey, Consumer<RecipeDisplayEntry> consumer, Operation<Void> original, @Local(argsOnly = true) ServerPlayer player) {
        //? if >=1.21.11 {
        String id = recipeKey.identifier().toString();
        //?} else
        /*String id = recipeKey.location().toString();*/
        if (!Permissions.check(player, PERM + "." + id, true)) {
            return;
        }
        original.call(instance, recipeKey, consumer);
    }
    //?}
}
