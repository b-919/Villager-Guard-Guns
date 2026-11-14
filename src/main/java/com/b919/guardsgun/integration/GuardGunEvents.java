package com.b919.guardsgun.integration;

import com.b919.guardsgun.Config;
import com.b919.guardsgun.Guardsgun;
import com.b919.guardsgun.mixin.MobAccessor;
import com.tacz.guns.api.item.IGun;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Guardsgun.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuardGunEvents {
    private static final String TAG_GUN_PROCESSED = "guardsgun:gun_processed";
    private static final int MAX_ARM_TRIES = 100;
    private static final int VERIFY_TICKS = 40;

    private static final Map<Guard, ArmState> ARM_STATES = new WeakHashMap<>();

    private GuardGunEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Guard guard) || guard.level().isClientSide()) {
            return;
        }

        GuardGunAmmo.ensureAmmoBoxSlot(guard);

        ArmState state = stateOf(guard);
        if (!state.goalsAdded) {
            state.goalsAdded = true;

            ((MobAccessor) guard).guardsgun$getGoalSelector().addGoal(1, new GuardRefillAmmoGoal(guard));
            ((MobAccessor) guard).guardsgun$getGoalSelector().addGoal(2, new GuardGunAiGoal(guard));
        }

        if (Config.enableGuns && !isGunProcessed(guard)) {
            if (hasGun(guard)) {
                markGunProcessed(guard);
            } else {
                state.armTries = 0;
                state.pendingGun = true;
            }
        }
    }

    private static ArmState stateOf(Guard guard) {
        return ARM_STATES.computeIfAbsent(guard, ignored -> new ArmState());
    }

    private static boolean isGunProcessed(Guard guard) {
        return guard.getPersistentData().getBoolean(TAG_GUN_PROCESSED);
    }

    private static void markGunProcessed(Guard guard) {
        guard.getPersistentData().putBoolean(TAG_GUN_PROCESSED, true);
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
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Guard guard) || guard.level().isClientSide()) {
            return;
        }
        if (!Config.enableGuns) {
            return;
        }

        ArmState state = ARM_STATES.get(guard);
        if (state == null) {
            return;
        }

        if (state.pendingGun) {
            if (GuardGunEquipment.equipGuard(guard, guard.getRandom())) {
                state.armTries = 0;
                state.verifyTicks = VERIFY_TICKS;
                state.pendingGun = false;
            } else {
                state.armTries++;
                if (state.armTries >= MAX_ARM_TRIES) {
                    state.armTries = 0;
                    state.pendingGun = false;
                }
            }
            return;
        }

        if (state.verifyTicks > 0) {
            if (!(guard.getMainHandItem().getItem() instanceof IGun)) {
                state.verifyTicks = 0;
                state.armTries = 0;
                state.pendingGun = true;
            } else if (state.verifyTicks <= 1) {
                state.verifyTicks = 0;
                markGunProcessed(guard);
            } else {
                state.verifyTicks--;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
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

    private static final class ArmState {
        private boolean goalsAdded;
        private boolean pendingGun;
        private int armTries;
        private int verifyTicks;
    }
}
