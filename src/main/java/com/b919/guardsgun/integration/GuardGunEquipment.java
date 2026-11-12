package com.b919.guardsgun.integration;

import com.b919.guardsgun.Config;
import com.b919.guardsgun.WeaponEntry;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.common.entities.Guard;

import java.util.Optional;

public final class GuardGunEquipment {
    private GuardGunEquipment() {
    }

    public static void equipGuard(Guard guard, RandomSource random) {
        if (!Config.enableGuns) {
            return;
        }

        WeaponEntry weapon = pickWeightedWeapon(random);
        if (weapon == null) {
            return;
        }

        Optional<CommonGunIndex> index = TimelessAPI.getCommonGunIndex(weapon.id());
        if (index.isEmpty()) {

            return;
        }

        int magazineSize = index.get().getGunData().getAmmoAmount();
        ResourceLocation ammoId = index.get().getGunData().getAmmoId();

        ItemStack gun = GunItemBuilder.create()
                .setId(weapon.id())
                .setAmmoCount(magazineSize)
                .build();
        if (gun.isEmpty() || IGun.getIGunOrNull(gun) == null) {
            return;
        }

        guard.setItemSlot(EquipmentSlot.MAINHAND, gun);

        if (ammoId != null) {
            ItemStack box = GuardGunAmmo.createAmmoBox(ammoId);
            if (!box.isEmpty()) {
                GuardGunAmmo.setAmmoBox(guard, box);
            }
        }

    }

    private static WeaponEntry pickWeightedWeapon(RandomSource random) {
        if (Config.weaponPool.isEmpty()) {
            return null;
        }
        int total = 0;
        for (WeaponEntry entry : Config.weaponPool) {
            total += Math.max(entry.weight(), 0);
        }
        if (total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        int cursor = 0;
        for (WeaponEntry entry : Config.weaponPool) {
            cursor += Math.max(entry.weight(), 0);
            if (roll < cursor) {
                return entry;
            }
        }
        return Config.weaponPool.getLast();
    }
}
