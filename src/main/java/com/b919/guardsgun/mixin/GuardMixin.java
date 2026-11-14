package com.b919.guardsgun.mixin;

import com.b919.guardsgun.integration.GuardGunAmmo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.guardvillagers.entities.Guard;

@Mixin(Guard.class)
public abstract class GuardMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void guardsgun$expandInventory(EntityType<? extends Guard> type, Level world, CallbackInfo ci) {
        GuardGunAmmo.ensureAmmoBoxSlot((Guard) (Object) this);
    }
}
