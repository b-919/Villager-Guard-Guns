package com.b919.guardsgun.integration;

import com.b919.guardsgun.Config;
import com.b919.guardsgun.Guardsgun;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.IGun;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.UUID;

@EventBusSubscriber(modid = Guardsgun.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class GuardGunEvents {
    private GuardGunEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Guard guard) || guard.level().isClientSide()) {
            return;
        }

        GuardGunAmmo.ensureAmmoBoxSlot(guard);

        if (!guard.getData(Guardsgun.HAS_GUN_GOAL.get())) {
            guard.setData(Guardsgun.HAS_GUN_GOAL.get(), true);

            guard.goalSelector.addGoal(1, new GuardRefillAmmoGoal(guard));
            guard.goalSelector.addGoal(2, new GuardGunAiGoal(guard));
        }

        if (Config.enableGuns && !guard.getData(Guardsgun.GUN_PROCESSED.get())) {
            if (hasGun(guard)) {
                guard.setData(Guardsgun.GUN_PROCESSED.get(), true);
            } else {
                guard.setData(Guardsgun.GUN_ARM_TRIES.get(), 0);
                guard.setData(Guardsgun.PENDING_GUN.get(), true);
            }
        }
    }

    private static boolean hasGun(Guard guard) {
        if (guard.getMainHandItem().getItem() instanceof IGun) {
            return true;
        }
        for (int slot = 0; slot < guard.guardInventory.getContainerSize(); slot++) {
            if (guard.guardInventory.getItem(slot).getItem() instanceof IGun) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Guard guard) || guard.level().isClientSide()) {
            return;
        }
        if (!Config.enableGuns) {
            return;
        }

        if (guard.getData(Guardsgun.PENDING_GUN.get())) {
            if (GuardGunEquipment.equipGuard(guard, guard.getRandom())) {
                guard.setData(Guardsgun.GUN_ARM_TRIES.get(), 0);
                guard.setData(Guardsgun.GUN_VERIFY_TICKS.get(), 40);
                guard.removeData(Guardsgun.PENDING_GUN.get());
            } else {
                int tries = guard.getData(Guardsgun.GUN_ARM_TRIES.get()) + 1;
                if (tries >= 100) {
                    guard.setData(Guardsgun.GUN_ARM_TRIES.get(), 0);
                    guard.removeData(Guardsgun.PENDING_GUN.get());
                } else {
                    guard.setData(Guardsgun.GUN_ARM_TRIES.get(), tries);
                }
            }
            return;
        }

        int verifyTicks = guard.getData(Guardsgun.GUN_VERIFY_TICKS.get());
        if (verifyTicks > 0) {
            if (!(guard.getMainHandItem().getItem() instanceof IGun)) {
                guard.setData(Guardsgun.GUN_VERIFY_TICKS.get(), 0);
                guard.setData(Guardsgun.GUN_ARM_TRIES.get(), 0);
                guard.setData(Guardsgun.PENDING_GUN.get(), true);
            } else if (verifyTicks <= 1) {
                guard.setData(Guardsgun.GUN_VERIFY_TICKS.get(), 0);
                guard.setData(Guardsgun.GUN_PROCESSED.get(), true);
            } else {
                guard.setData(Guardsgun.GUN_VERIFY_TICKS.get(), verifyTicks - 1);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Guard guard) || guard.level().isClientSide()) {
            return;
        }
        for (int slot = 0; slot < guard.guardInventory.getContainerSize(); slot++) {
            if (GuardGunAmmo.isInfiniteAmmoBox(guard.guardInventory.getItem(slot))) {
                guard.guardInventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Guard) {
            event.getDrops().removeIf(drop -> GuardGunAmmo.isInfiniteAmmoBox(drop.getItem()));
        }
    }

    @SubscribeEvent
    public static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        if (!Config.enableGuns || !Config.friendlyFire) {
            return;
        }
        LivingEntity attacker = event.getAttacker();
        if (attacker instanceof Guard guard
                && event.getHurtEntity() instanceof LivingEntity target
                && isFriendlyTarget(guard, target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!Config.enableGuns || !Config.friendlyFire) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null && event.getSource().getDirectEntity() instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }
        if (attacker instanceof Guard guard && isFriendlyTarget(guard, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static boolean isFriendlyTarget(Guard guard, LivingEntity target) {
        if (target == guard) {
            return false;
        }
        if (guard.isAlliedTo(target) || isGuardVillagersProtected(target)) {
            return true;
        }
        if (target instanceof Player player) {
            UUID ownerId = guard.getOwnerId();
            return (ownerId != null && ownerId.equals(player.getUUID()))
                    || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
        }
        return false;
    }

    private static boolean isGuardVillagersProtected(LivingEntity target) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        return id != null && GuardConfig.COMMON.mobsGuardsProtectHurt.get().contains(id.toString());
    }
}
