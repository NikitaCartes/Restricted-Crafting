package xyz.nikitacartes.restrictedcrafting.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Slot.class)
public class SlotMixin {

    @WrapMethod(method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z")
    private boolean addRestrictions(PlayerEntity playerEntity, Operation<Boolean> original) {
        Slot slot = (Slot)(Object)this;
        if (!(slot instanceof CraftingResultSlot)) {
            return original.call(playerEntity);
        }
        if (!Permissions.check(playerEntity.getCommandSource(), "restricted-crafting." + Registries.ITEM.getEntry(slot.getStack().getItem()).getIdAsString(), true)) {
            playerEntity.sendMessage(Text.translatableWithFallback("text.restricted-crafting.restricted", "You do not have permission to craft this item!").formatted(Formatting.RED), true);
            // playerEntity.getCommandSource().sendError(Text.translatableWithFallback("text.restricted-crafting.restricted", "You do not have permission to craft this item!"));
            return false;
        }
        return true;
    }
}