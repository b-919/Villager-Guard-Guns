package com.b919.guardsgun.integration;

import com.b919.guardsgun.AmmoMode;
import com.b919.guardsgun.Config;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.block.entity.AmmoStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.common.entities.Guard;

import java.util.EnumSet;

public class GuardRefillAmmoGoal extends Goal {
    private static final int SEARCH_RADIUS = 12;
    private static final double REACH_DISTANCE_SQR = 4.0;

    private final Guard guard;

    private AmmoStorageBlockEntity storage;

    private int scanCooldown;

    public GuardRefillAmmoGoal(Guard guard) {
        this.guard = guard;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Config.refillAmmo) {
            return false;
        }

        if (guard.getTarget() != null) {
            return false;
        }
        if (!Config.refillAtNight && guard.level().isNight()) {
            return false;
        }

        ResourceLocation ammoId = GuardGunAmmo.getGunAmmoId(guard.getMainHandItem());
        if (ammoId == null) {
            return false;
        }

        boolean wrongAmmo = Config.ammoMode != AmmoMode.INFINITEAMMO
                && GuardGunAmmo.hasWrongAmmoBox(guard, ammoId);
        boolean needsRefill = Config.ammoMode == AmmoMode.NORMAL
                && GuardGunAmmo.isBoxLow(GuardGunAmmo.getAmmoBox(guard), ammoId);
        if (!wrongAmmo && !needsRefill) {
            return false;
        }

        if (storage != null) {
            if (isUsable(storage, ammoId, wrongAmmo)) {
                return true;
            }

            storage = null;
            scanCooldown = 0;
        }

        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }

        storage = findNearestStorage(ammoId, wrongAmmo);
        if (storage == null) {

            scanCooldown = Config.refillCooldownTicks;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (storage == null || !Config.refillAmmo) {
            return false;
        }
        if (guard.getTarget() != null) {
            return false;
        }
        if (!Config.refillAtNight && guard.level().isNight()) {
            return false;
        }

        ResourceLocation ammoId = GuardGunAmmo.getGunAmmoId(guard.getMainHandItem());
        if (ammoId == null) {
            return false;
        }

        boolean wrongAmmo = Config.ammoMode != AmmoMode.INFINITEAMMO
                && GuardGunAmmo.hasWrongAmmoBox(guard, ammoId);
        boolean needsRefill = Config.ammoMode == AmmoMode.NORMAL
                && GuardGunAmmo.isBoxLow(GuardGunAmmo.getAmmoBox(guard), ammoId);
        if (!wrongAmmo && !needsRefill) {
            return false;
        }

        if (!isUsable(storage, ammoId, wrongAmmo)) {

            storage = null;
            scanCooldown = 0;
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        if (storage == null) {
            return;
        }
        BlockPos pos = storage.getBlockPos();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        if (guard.distanceToSqr(cx, cy, cz) > REACH_DISTANCE_SQR) {
            guard.getLookControl().setLookAt(cx, cy, cz);
            guard.getNavigation().moveTo(cx, cy, cz, 1.0D);
            return;
        }
        guard.getNavigation().stop();

        ResourceLocation ammoId = GuardGunAmmo.getGunAmmoId(guard.getMainHandItem());
        if (ammoId == null) {
            return;
        }
        if (Config.ammoMode != AmmoMode.INFINITEAMMO && GuardGunAmmo.hasWrongAmmoBox(guard, ammoId)) {
            exchange(ammoId);
        } else {
            refill(ammoId);
        }
    }

    private void refill(ResourceLocation ammoId) {
        ItemStack box = GuardGunAmmo.getAmmoBox(guard);
        if (storage == null || !(box.getItem() instanceof IAmmoBox iBox)) {
            storage = null;
            scanCooldown = 0;
            return;
        }
        if (GuardGunAmmo.isInfiniteAmmoBox(box)) {

            storage = null;
            scanCooldown = 0;
            return;
        }

        int current = iBox.getAmmoCount(box);
        int capacity = GuardGunAmmo.getBoxCapacity(box, ammoId);
        int wanted = Math.max(0, capacity - current);
        if (wanted <= 0) {

            return;
        }

        ItemStack extracted = storage.extractAmmo(ammoId, wanted, false);
        int got = extracted.getCount();
        if (got <= 0) {

            guard.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            storage = null;
            scanCooldown = 0;
            return;
        }

        iBox.setAmmoId(box, ammoId);
        iBox.setAmmoCount(box, current + got);
        guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);

        if (!hasAmmo(storage, ammoId)) {

            storage = null;
            scanCooldown = 0;
        }
    }

    private void exchange(ResourceLocation gunAmmoId) {
        ItemStack box = GuardGunAmmo.getAmmoBox(guard);
        if (storage == null
                || !(box.getItem() instanceof IAmmoBox iBox)
                || GuardGunAmmo.isInfiniteAmmoBox(box)
                || gunAmmoId.equals(iBox.getAmmoId(box))) {

            return;
        }

        if (!hasAmmo(storage, gunAmmoId)) {

            guard.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            storage = null;
            scanCooldown = 0;
            return;
        }

        ResourceLocation boxAmmoId = iBox.getAmmoId(box);
        int boxCount = iBox.getAmmoCount(box);
        if (boxCount > 0) {
            if (boxAmmoId == null) {

                storage = null;
                scanCooldown = 0;
                return;
            }
            ItemStack wrongAmmo = AmmoItemBuilder.create().setId(boxAmmoId).setCount(boxCount).build();
            int deposited = storage.insertAmmo(wrongAmmo, false);
            if (deposited < boxCount) {

                iBox.setAmmoCount(box, boxCount - deposited);
                storage = null;
                scanCooldown = 0;
                return;
            }
        }

        iBox.setAmmoId(box, gunAmmoId);
        iBox.setAmmoCount(box, 0);

        int capacity = GuardGunAmmo.getBoxCapacity(box, gunAmmoId);
        ItemStack extracted = storage.extractAmmo(gunAmmoId, capacity, false);
        int received = extracted.getCount();
        iBox.setAmmoCount(box, received);
        guard.playSound(received > 0 ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO,
                1.0F, 1.0F);

        if (!hasAmmo(storage, gunAmmoId)) {

            storage = null;
            scanCooldown = 0;
        }
    }

    @Override
    public void stop() {
        guard.getNavigation().stop();

    }

    private boolean isUsable(AmmoStorageBlockEntity candidate, ResourceLocation ammoId, boolean exchange) {
        if (candidate.isRemoved() || !hasAmmo(candidate, ammoId)) {
            return false;
        }
        return !exchange || canDepositWrongAmmo(candidate);
    }

    private boolean canDepositWrongAmmo(AmmoStorageBlockEntity candidate) {
        ItemStack box = GuardGunAmmo.getAmmoBox(guard);
        if (!(box.getItem() instanceof IAmmoBox iBox) || GuardGunAmmo.isInfiniteAmmoBox(box)) {
            return false;
        }
        int count = iBox.getAmmoCount(box);
        if (count <= 0) {
            return true;
        }
        ResourceLocation boxAmmoId = iBox.getAmmoId(box);
        if (boxAmmoId == null) {
            return false;
        }
        ItemStack wrongAmmo = AmmoItemBuilder.create().setId(boxAmmoId).setCount(count).build();
        return candidate.getRemainingCapacity(wrongAmmo) >= count;
    }

    private static boolean hasAmmo(AmmoStorageBlockEntity candidate, ResourceLocation ammoId) {
        return !candidate.extractAmmo(ammoId, 1, true).isEmpty();
    }

    private AmmoStorageBlockEntity findNearestStorage(ResourceLocation ammoId, boolean exchange) {
        BlockPos center = guard.blockPosition();
        AmmoStorageBlockEntity nearest = null;
        double nearestSqr = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS),
                center.offset(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS))) {
            if (guard.level().getBlockEntity(pos) instanceof AmmoStorageBlockEntity candidate
                    && isUsable(candidate, ammoId, exchange)) {
                double distSqr = pos.distSqr(center);
                if (distSqr < nearestSqr) {
                    nearestSqr = distSqr;
                    nearest = candidate;
                }
            }
        }
        return nearest;
    }
}
