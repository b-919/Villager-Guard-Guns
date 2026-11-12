package com.b919.guardsgun.integration;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import tallestegg.guardvillagers.common.entities.Guard;

public class GuardItemHandler extends InvWrapper {
    public GuardItemHandler(Guard guard) {
        super(guard.guardInventory);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == GuardGunAmmo.AMMO_BOX_SLOT && GuardGunAmmo.isInfiniteAmmoBox(getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot == GuardGunAmmo.AMMO_BOX_SLOT && GuardGunAmmo.isInfiniteAmmoBox(stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot == GuardGunAmmo.AMMO_BOX_SLOT
                && (GuardGunAmmo.isInfiniteAmmoBox(getStackInSlot(slot))
                || GuardGunAmmo.isInfiniteAmmoBox(stack))) {
            return;
        }
        super.setStackInSlot(slot, stack);
    }
}
