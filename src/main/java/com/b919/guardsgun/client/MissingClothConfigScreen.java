package com.b919.guardsgun.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

/** Shown instead of the config screen when Cloth Config is not installed. */
public class MissingClothConfigScreen extends Screen {
    private final Screen parent;

    protected MissingClothConfigScreen(Screen parent) {
        super(Component.translatable("guardsgun.config.title"));
        this.parent = parent;
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new MissingClothConfigScreen(parent));
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 100, this.height / 2 + 40, 200, 20)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.missingCloth"),
                this.width / 2, this.height / 2 - 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.missingCloth.hint"),
                this.width / 2, this.height / 2, 0xA0A0A0);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}
