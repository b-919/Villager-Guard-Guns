package com.b919.guardsgun.mixin;

import com.b919.guardsgun.integration.GuardItemHandler;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.entities.Guard;

@Mixin(Guard.class)
public abstract class GuardCapabilityMixin {
    @Unique
    private LazyOptional<?> guardsgun$itemHandler;

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    private <T> void guardsgun$getItemHandler(Capability<T> capability, @Nullable Direction facing,
                                              CallbackInfoReturnable<LazyOptional<T>> cir) {
        Guard guard = (Guard) (Object) this;
        if (guard.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER) {
            LazyOptional<?> handler = this.guardsgun$itemHandler;
            if (handler == null) {
                handler = LazyOptional.of(() -> new GuardItemHandler(guard));
                this.guardsgun$itemHandler = handler;
            }
            cir.setReturnValue(handler.cast());
        }
    }

    @Inject(method = "invalidateCaps", at = @At("TAIL"), remap = false)
    private void guardsgun$invalidateItemHandler(CallbackInfo ci) {
        LazyOptional<?> handler = this.guardsgun$itemHandler;
        if (handler != null) {
            this.guardsgun$itemHandler = null;
            handler.invalidate();
        }
    }
}
