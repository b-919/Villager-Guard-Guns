package com.b919.guardsgun.integration;

import com.b919.guardsgun.Config;
import com.b919.guardsgun.GunType;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class GuardGunAccuracy {
    private GuardGunAccuracy() {
    }

    public static double getMultiplier(ItemStack gun) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return Config.defaultAccuracyMultiplier;
        }
        String rawType = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun))
                .map(index -> index.getType())
                .orElse(null);
        if (rawType == null) {
            return Config.defaultAccuracyMultiplier;
        }
        String type = GunType.fromTaCzType(rawType).getTaCzName();
        return Config.accuracyOverrides.getOrDefault(type, Config.defaultAccuracyMultiplier);
    }

    public static void applyTo(LivingEntity shooter) {
        ItemStack gun = shooter.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return;
        }

        double multiplier = getMultiplier(gun);
        if (multiplier == 1.0) {
            return;
        }

        AttachmentCacheProperty cache = IGunOperator.fromLivingEntity(shooter).getCacheProperty();
        if (cache == null) {
            return;
        }

        Map<InaccuracyType, Float> current = cache.getCache(GunProperties.INACCURACY);
        if (current == null) {
            return;
        }

        Map<InaccuracyType, Float> modified = new HashMap<>();
        current.forEach((type, value) -> modified.put(type, value * (float) multiplier));

        cache.setCache(GunProperties.INACCURACY, modified);
    }
}
