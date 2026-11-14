package com.b919.guardsgun.integration;

import com.b919.guardsgun.AmmoMode;
import com.b919.guardsgun.Config;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.entities.Guard;

public final class GuardGunAmmo {

    public static final int AMMO_BOX_SLOT = 6;

    private GuardGunAmmo() {
    }

    public static void ensureAmmoBoxSlot(Guard guard) {
        if (guard.guardInventory.getContainerSize() >= AMMO_BOX_SLOT + 1) {
            return;
        }
        SimpleContainer old = guard.guardInventory;
        SimpleContainer larger = new SimpleContainer(AMMO_BOX_SLOT + 1);
        for (int i = 0; i < old.getContainerSize(); i++) {
            larger.setItem(i, old.getItem(i));
        }

        larger.addListener(guard);
        guard.guardInventory = larger;
    }

    public static ItemStack getAmmoBox(Guard guard) {
        return guard.guardInventory.getItem(AMMO_BOX_SLOT);
    }

    public static void setAmmoBox(Guard guard, ItemStack box) {
        guard.guardInventory.setItem(AMMO_BOX_SLOT, box);
    }

    public static boolean isInfiniteAmmoBox(ItemStack box) {
        if (!(box.getItem() instanceof IAmmoBox iBox)) {
            return false;
        }
        return iBox.isCreative(box) || iBox.isAllTypeCreative(box);
    }

    public static boolean hasWrongAmmoBox(Guard guard, ResourceLocation gunAmmoId) {
        ItemStack box = getAmmoBox(guard);
        if (!(box.getItem() instanceof IAmmoBox iBox) || isInfiniteAmmoBox(box)) {
            return false;
        }
        return !gunAmmoId.equals(iBox.getAmmoId(box));
    }

    public static void ensureCreativeBoxMatches(Guard guard, ResourceLocation gunAmmoId) {
        ItemStack box = getAmmoBox(guard);
        if (!(box.getItem() instanceof IAmmoBox iBox)
                || !iBox.isCreative(box)
                || iBox.isAllTypeCreative(box)) {
            return;
        }
        if (!gunAmmoId.equals(iBox.getAmmoId(box))) {
            iBox.setAmmoId(box, gunAmmoId);
        }
    }

    public static ItemStack createAmmoBox(ResourceLocation ammoId) {
        ItemStack box = new ItemStack(ModItems.AMMO_BOX.get());
        if (box.getItem() instanceof IAmmoBox iBox) {
            iBox.setAmmoId(box, ammoId);
            if (Config.ammoMode == AmmoMode.INFINITEMAGS) {
                iBox.setCreative(box, false);
            } else {
                iBox.setAmmoLevel(box, Config.ammoBoxLevel);
                int capacity = getBoxCapacity(box, ammoId);
                int ammoCount = (int) Math.round(capacity * (Config.ammoFillPercent / 100.0));
                iBox.setAmmoCount(box, ammoCount);
            }
        }
        return box;
    }

    public static ResourceLocation getGunAmmoId(ItemStack gun) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return null;
        }
        return TimelessAPI.getCommonGunIndex(iGun.getGunId(gun))
                .map(index -> index.getGunData().getAmmoId())
                .orElse(null);
    }

    public static int getBoxCapacity(ItemStack box, ResourceLocation ammoId) {
        if (!(box.getItem() instanceof IAmmoBox iBox) || isInfiniteAmmoBox(box)) {
            return Integer.MAX_VALUE;
        }
        int stackSize = TimelessAPI.getCommonAmmoIndex(ammoId)
                .map(index -> index.getStackSize())
                .orElse(64);
        int levelMultiplier = iBox.getAmmoLevel(box) + 1;
        return stackSize * SyncConfig.AMMO_BOX_STACK_SIZE.get() * levelMultiplier;
    }

    public static boolean isBoxLow(ItemStack box, ResourceLocation ammoId) {
        if (!(box.getItem() instanceof IAmmoBox iBox) || isInfiniteAmmoBox(box)) {
            return false;
        }
        int current = iBox.getAmmoCount(box);
        int capacity = getBoxCapacity(box, ammoId);
        return capacity > 0 && (long) current * 100 <= (long) capacity * Config.refillThresholdPercent;
    }

    public static boolean hasReserveAmmo(LivingEntity shooter, ItemStack gun) {
        if (!(shooter instanceof Guard guard)) {
            return false;
        }
        ItemStack box = getAmmoBox(guard);
        if (box.getItem() instanceof IAmmoBox iBox && iBox.isAmmoBoxOfGun(gun, box)) {
            return isInfiniteAmmoBox(box) || iBox.getAmmoCount(box) > 0;
        }
        return false;
    }

    public static void refillMagazine(LivingEntity shooter) {
        if (Config.ammoMode != AmmoMode.INFINITEAMMO) {
            return;
        }
        ItemStack gun = shooter.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return;
        }
        TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).ifPresent(index -> {
            int magazine = index.getGunData().getAmmoAmount();
            if (iGun.getCurrentAmmoCount(gun) < magazine) {
                iGun.setCurrentAmmoCount(gun, magazine);
            }
        });
    }
}
