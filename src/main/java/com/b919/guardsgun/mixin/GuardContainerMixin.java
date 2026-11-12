package com.b919.guardsgun.mixin;

import com.b919.guardsgun.integration.GuardAmmoBoxSlot;
import com.b919.guardsgun.integration.GuardGunAmmo;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.common.entities.GuardContainer;

@Mixin(GuardContainer.class)
public abstract class GuardContainerMixin {
    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ltallestegg/guardvillagers/common/entities/GuardContainer;addSlot"
                            + "(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;",
                    ordinal = 5,
                    shift = At.Shift.AFTER
            )
    )
    private void guardsgun$addAmmoBoxSlot(int id, Inventory playerInventory, Container guardInventory,
                                          Guard guard, CallbackInfo ci) {
        ((AbstractContainerMenu) (Object) this).addSlot(
                new GuardAmmoBoxSlot(guardInventory, GuardGunAmmo.AMMO_BOX_SLOT, 152, 44));
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void guardsgun$blockInfiniteAmmoBoxQuickMove(Player playerIn, int index,
                                                         CallbackInfoReturnable<ItemStack> cir) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (index < 0 || index >= menu.slots.size()) {
            return;
        }
        Slot slot = menu.getSlot(index);
        if (slot instanceof GuardAmmoBoxSlot && GuardGunAmmo.isInfiniteAmmoBox(slot.getItem())) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
