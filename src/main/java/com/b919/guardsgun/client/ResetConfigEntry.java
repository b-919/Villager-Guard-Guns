package com.b919.guardsgun.client;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/** Cloth Config entry that opens the reset confirmation screen. */
public class ResetConfigEntry extends AbstractConfigListEntry<Void> {
    private final Button button;

    public ResetConfigEntry(Component fieldName, Screen parent) {
        super(fieldName, false);
        this.button = Button.builder(Component.translatable("guardsgun.config.reset.button"), pressed -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new ConfirmResetScreen(minecraft.screen, parent));
        }).bounds(0, 0, 160, 20).build();
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public Optional<Void> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public void save() {
    }

    @Override
    public boolean isEdited() {
        return false;
    }

    @Override
    public int getItemHeight() {
        return 24;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean isHovered, float delta) {
        button.setX(x + 4);
        button.setY(y + 2);
        button.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of(button);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.button.mouseClicked(mouseX, mouseY, button);
    }
}
