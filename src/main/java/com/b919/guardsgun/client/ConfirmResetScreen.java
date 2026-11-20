package com.b919.guardsgun.client;

import com.b919.guardsgun.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/** Confirmation screen shown before resetting every GuardsGun setting. */
public class ConfirmResetScreen extends Screen {
    private final Screen configScreen;
    private final Screen parent;

    protected ConfirmResetScreen(Screen configScreen, Screen parent) {
        super(Component.translatable("guardsgun.config.reset.confirm.title"));
        this.configScreen = configScreen;
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("guardsgun.config.reset.confirm.button"),
                        button -> reset())
                .bounds(this.width / 2 - 104, this.height / 2 + 16, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(this.width / 2 + 4, this.height / 2 + 16, 100, 20)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("guardsgun.config.reset.confirm.message1"),
                this.width / 2, this.height / 2 - 30, 0xFFAA00);
        graphics.drawCenteredString(this.font,
                Component.translatable("guardsgun.config.reset.confirm.message2"),
                this.width / 2, this.height / 2 - 18, 0xA0A0A0);
    }

    private void reset() {
        Config.resetToDefaults();
        if (this.minecraft != null) {
            this.minecraft.setScreen(GuardsgunClothConfigScreen.create(this.parent));
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.configScreen);
        }
    }
}
