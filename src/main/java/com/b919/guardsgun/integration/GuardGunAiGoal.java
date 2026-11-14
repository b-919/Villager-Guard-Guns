package com.b919.guardsgun.integration;

import com.b919.guardsgun.AmmoMode;
import com.b919.guardsgun.Config;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.entities.Guard;

import java.util.EnumSet;

public class GuardGunAiGoal extends Goal {
    private static final float ATTACK_RANGE = 20.0F;
    private static final float ATTACK_RANGE_SQR = ATTACK_RANGE * ATTACK_RANGE;

    private final Guard guard;
    private final IGunOperator operator;

    public GuardGunAiGoal(Guard guard) {
        this.guard = guard;
        this.operator = IGunOperator.fromLivingEntity(guard);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Config.enableGuns) {
            return false;
        }
        LivingEntity target = guard.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return guard.getMainHandItem().getItem() instanceof IGun;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {

        operator.draw(guard::getMainHandItem);

        GuardGunAccuracy.applyTo(guard);
    }

    @Override
    public void tick() {
        LivingEntity target = guard.getTarget();
        if (target == null) {
            return;
        }

        guard.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (guard.distanceToSqr(target) > ATTACK_RANGE_SQR) {
            guard.getNavigation().moveTo(target, 1.0D);
        } else {
            guard.getNavigation().stop();
        }

        ItemStack gun = guard.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return;
        }

        GuardGunAmmo.refillMagazine(guard);

        ReloadState.StateType reloadState = operator.getSynReloadState().getStateType();
        if (Config.ammoMode != AmmoMode.INFINITEAMMO
                && reloadState == ReloadState.StateType.NOT_RELOADING
                && iGun.getCurrentAmmoCount(gun) <= 0) {

            ResourceLocation gunAmmoId = GuardGunAmmo.getGunAmmoId(gun);
            if (gunAmmoId != null) {
                GuardGunAmmo.ensureCreativeBoxMatches(guard, gunAmmoId);
            }
            if (GuardGunAmmo.hasReserveAmmo(guard, gun)) {
                operator.reload();
                return;
            }
        }

        if (guard.distanceToSqr(target) <= ATTACK_RANGE_SQR && guard.getSensing().hasLineOfSight(target)) {
            ShootResult result = operator.shoot(guard::getXRot, guard::getYHeadRot);

            if (result == ShootResult.NEED_BOLT) {
                operator.bolt();
            }
        }
    }
}
