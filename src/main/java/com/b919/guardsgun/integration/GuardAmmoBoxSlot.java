package com.b919.guardsgun.integration;

import com.b919.guardsgun.AmmoMode;
import com.b919.guardsgun.Config;
import com.b919.guardsgun.Guardsgun;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuardAmmoBoxSlot extends Slot {
    private static final ResourceLocation EMPTY_AMMO_BOX_SLOT =
            ResourceLocation.fromNamespaceAndPath(Guardsgun.MODID, "item/empty_ammo_box_slot");

    public GuardAmmoBoxSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
        this.setBackground(InventoryMenu.BLOCK_ATLAS, EMPTY_AMMO_BOX_SLOT);
    }

    @Override
    public boolean isActive() {
        return Config.ammoMode == AmmoMode.NORMAL && !GuardGunAmmo.isInfiniteAmmoBox(getItem());
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return isActive() && stack.getItem() instanceof IAmmoBox
                && !GuardGunAmmo.isInfiniteAmmoBox(stack);
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return isActive() && !GuardGunAmmo.isInfiniteAmmoBox(getItem());
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        if (GuardGunAmmo.isInfiniteAmmoBox(getItem())) {
            return ItemStack.EMPTY;
        }
        return super.remove(amount);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
