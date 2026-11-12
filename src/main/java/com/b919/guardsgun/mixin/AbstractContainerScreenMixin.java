package com.b919.guardsgun.mixin;

import com.b919.guardsgun.integration.GuardAmmoBoxSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void guardsgun$beginSmoothAmmoSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (slot instanceof GuardAmmoBoxSlot && slot.getItem().isEmpty()) {
            Minecraft minecraft = Minecraft.getInstance();

            boolean mipmap = minecraft.options.mipmapLevels().get() > 0;

            minecraft.getTextureManager()
                    .getTexture(InventoryMenu.BLOCK_ATLAS)
                    .setBlurMipmap(!mipmap, mipmap);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void guardsgun$endSmoothAmmoSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (slot instanceof GuardAmmoBoxSlot && slot.getItem().isEmpty()) {
            Minecraft.getInstance()
                    .getTextureManager()
                    .getTexture(InventoryMenu.BLOCK_ATLAS)
                    .restoreLastBlurMipmap();
        }
    }
}
