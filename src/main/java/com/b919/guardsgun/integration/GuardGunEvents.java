package com.b919.guardsgun.integration;

import com.b919.guardsgun.Config;
import com.b919.guardsgun.Guardsgun;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.common.entities.Guard;

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

        if (Config.enableGuns && !event.loadedFromDisk()) {
            guard.setData(Guardsgun.PENDING_GUN.get(), true);
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Guard guard) || guard.level().isClientSide()) {
            return;
        }

        if (guard.getData(Guardsgun.PENDING_GUN.get())) {
            guard.removeData(Guardsgun.PENDING_GUN.get());
            GuardGunEquipment.equipGuard(guard, guard.getRandom());
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
}
