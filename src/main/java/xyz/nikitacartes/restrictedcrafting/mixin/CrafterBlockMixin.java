package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.luckperms.api.util.Tristate;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.defaultGroup;

@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {

    @Inject(method = "craft(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/CrafterBlockEntity;setCraftingTicksRemaining(I)V"),
            cancellable = true)
    private void addRestrictions(BlockState state, ServerWorld world, BlockPos pos, CallbackInfo ci, @Local ItemStack itemStack, @Local CrafterBlockEntity crafterBlockEntity) {
        if (defaultGroup.getCachedData().getPermissionData().checkPermission("restricted-crafting.crafter." + Registries.ITEM.getEntry(itemStack.getItem()).getIdAsString()) == Tristate.FALSE) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            ci.cancel();
        }
    }
}
