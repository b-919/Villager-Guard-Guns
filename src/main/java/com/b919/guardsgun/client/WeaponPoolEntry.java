package com.b919.guardsgun.client;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Cloth Config entry that opens the icon-based weapon pool picker. */
public class WeaponPoolEntry extends AbstractConfigListEntry<List<String>> {
    private final List<String> value;
    private final List<String> defaultValue;
    private final Consumer<List<String>> saveConsumer;
    private final Button button;
    private boolean edited;

    public WeaponPoolEntry(Component fieldName, List<String> initial, List<String> defaultValue,
                           Consumer<List<String>> saveConsumer) {
        super(fieldName, false);
        this.value = new ArrayList<>(initial);
        this.defaultValue = new ArrayList<>(defaultValue);
        this.saveConsumer = saveConsumer;
        this.button = Button.builder(label(), pressed -> openPicker())
                .bounds(0, 0, 110, 20)
                .build();
    }

    private Component label() {
        return Component.translatable("guardsgun.config.weaponPool.button", value.size());
    }

    private void openPicker() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new WeaponPoolScreen(minecraft.screen, value, updated -> {
            value.clear();
            value.addAll(updated);
            edited = true;
            button.setMessage(label());
        }));
    }

    @Override
    public List<String> getValue() {
        return value;
    }

    @Override
    public Optional<List<String>> getDefaultValue() {
        return Optional.of(defaultValue);
    }

    @Override
    public void save() {
        saveConsumer.accept(List.copyOf(value));
    }

    @Override
    public boolean isEdited() {
        return edited;
    }

    @Override
    public int getItemHeight() {
        return 24;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean isHovered, float delta) {
        graphics.drawString(Minecraft.getInstance().font, getFieldName(), x + 4, y + 8, 0xFFFFFF);
        button.setX(x + entryWidth - 116);
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
