package com.b919.guardsgun.client;

import com.b919.guardsgun.GunType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/** Editor for accuracyOverrides. */
public class AccuracyOverridesScreen extends Screen {
    private static final int MARGIN = 16;
    private static final int GAP = 12;
    private static final int TYPE_TOP = 20;
    private static final int TYPE_HEIGHT = 54;
    private static final int ROW = 22;

    private final Screen parent;
    private final Consumer<List<String>> onApply;
    private final List<TypeCell> types = new ArrayList<>();
    private final List<GunCatalog.GunInfo> guns = new ArrayList<>();
    private final List<GunCatalog.GunInfo> filtered = new ArrayList<>();
    private final List<GunOverride> overrides = new ArrayList<>();
    private final List<String> unknownTypeEntries = new ArrayList<>();

    private int typeCellWidth;
    private int panelWidth;
    private int listHeight;

    private GunListWidget gunList;
    private OverrideListWidget overrideList;
    private EditBox searchBox;
    private EditBox multiplierBox;
    private Button addButton;
    private Button removeButton;

    private GunCatalog.GunInfo selectedGun;
    private GunOverride selectedOverride;

    private int iconTick;
    private int iconIndex;

    public AccuracyOverridesScreen(Screen parent, List<String> current, Consumer<List<String>> onApply) {
        super(Component.translatable("guardsgun.config.accuracy.title"));
        this.parent = parent;
        this.onApply = onApply;
        loadGuns();
        parseEntries(current);
    }

    private void loadGuns() {
        Map<GunType, List<ItemStack>> iconsByType = new EnumMap<>(GunType.class);
        for (GunCatalog.GunInfo gun : GunCatalog.collect()) {
            guns.add(gun);
            GunType type = GunType.fromTaCzType(gun.type());
            iconsByType.computeIfAbsent(type, key -> new ArrayList<>()).add(gun.icon());
        }
        guns.sort(Comparator.comparing(gun -> gun.name().getString(), String.CASE_INSENSITIVE_ORDER));
        filtered.addAll(guns);
        for (GunType type : GunType.values()) {
            types.add(new TypeCell(type, new ArrayList<>(iconsByType.getOrDefault(type, List.of()))));
        }
    }

    private void parseEntries(List<String> entries) {
        for (String entry : entries) {
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = entry.substring(0, separator).trim();
            Double value = parseMultiplier(entry.substring(separator + 1).trim());
            if (value == null) {
                continue;
            }
            if (key.contains(":")) {
                ResourceLocation id;
                try {
                    id = new ResourceLocation(key);
                } catch (Exception e) {
                    continue;
                }
                overrides.add(createOverride(id, value));
            } else {
                GunType type = GunType.fromTaCzType(key.toLowerCase(Locale.ROOT));
                boolean known = type != GunType.OTHER || key.equalsIgnoreCase(GunType.OTHER.getTaCzName());
                if (!known) {
                    unknownTypeEntries.add(entry);
                    continue;
                }
                for (TypeCell cell : types) {
                    if (cell.type == type) {
                        cell.initialValue = format(value);
                        break;
                    }
                }
            }
        }
    }

    private GunOverride createOverride(ResourceLocation id, double multiplier) {
        for (GunCatalog.GunInfo gun : guns) {
            if (gun.id().equals(id)) {
                return new GunOverride(id, multiplier, gun.icon(), gun.name());
            }
        }
        return new GunOverride(id, multiplier, new ItemStack(Items.BARRIER), Component.literal(id.toString()));
    }

    @Override
    protected void init() {
        typeCellWidth = Math.max(28, (this.width - MARGIN * 2) / types.size());
        for (int i = 0; i < types.size(); i++) {
            TypeCell cell = types.get(i);
            if (cell.box != null) {
                cell.initialValue = cell.box.getValue();
            }
            int x = MARGIN + i * typeCellWidth;
            cell.box = new EditBox(this.font, x + 2, TYPE_TOP + 34, Math.max(24, typeCellWidth - 4), 18,
                    Component.empty());
            cell.box.setFilter(value -> value.matches("\\d*(\\.\\d*)?"));
            cell.box.setMaxLength(6);
            cell.box.setValue(cell.initialValue);
            addRenderableWidget(cell.box);
        }

        panelWidth = Math.max(100, (this.width - MARGIN * 2 - GAP) / 2);
        int searchY = TYPE_TOP + TYPE_HEIGHT + 16;
        int listTop = searchY + 20;
        listHeight = Math.max(30, this.height - listTop - 46);

        searchBox = new EditBox(this.font, MARGIN, searchY, panelWidth, 16,
                Component.translatable("guardsgun.config.accuracy.search"));
        searchBox.setResponder(this::updateFilter);
        addRenderableWidget(searchBox);

        gunList = new GunListWidget(this.minecraft, panelWidth, listHeight, listTop, listTop + listHeight, ROW);
        gunList.setLeftPos(MARGIN);
        addRenderableWidget(gunList);
        gunList.setGuns(filtered);

        overrideList = new OverrideListWidget(this.minecraft, panelWidth, listHeight, listTop, listTop + listHeight, ROW);
        overrideList.setLeftPos(MARGIN + panelWidth + GAP);
        addRenderableWidget(overrideList);
        overrideList.setOverrides(overrides);

        int barY = this.height - 42;
        multiplierBox = new EditBox(this.font, MARGIN + 64, barY, 48, 18,
                Component.translatable("guardsgun.config.accuracy.multiplier"));
        multiplierBox.setFilter(value -> value.matches("\\d*(\\.\\d*)?"));
        multiplierBox.setMaxLength(6);
        multiplierBox.setValue("1.0");
        addRenderableWidget(multiplierBox);

        addButton = Button.builder(Component.translatable("guardsgun.config.accuracy.add"),
                        pressed -> addOrUpdate())
                .bounds(MARGIN + 120, barY, 70, 20)
                .build();
        addRenderableWidget(addButton);

        removeButton = Button.builder(Component.translatable("guardsgun.config.accuracy.remove"),
                        pressed -> removeSelected())
                .bounds(MARGIN + 198, barY, 70, 20)
                .build();
        addRenderableWidget(removeButton);

        addRenderableWidget(Button.builder(Component.translatable("guardsgun.config.accuracy.apply"),
                        pressed -> applyAndClose())
                .bounds(this.width / 2 - 104, this.height - 22, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), pressed -> onClose())
                .bounds(this.width / 2 + 4, this.height - 22, 100, 20)
                .build());

        updateButtons();
    }

    @Override
    public void tick() {
        if (++iconTick >= 20) {
            iconTick = 0;
            iconIndex++;
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xFF101010);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderDimBackground(graphics);
        graphics.fill(MARGIN - 4, TYPE_TOP - 6, this.width - MARGIN + 4,
                TYPE_TOP + TYPE_HEIGHT - 2, 0x66000000);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);

        for (int i = 0; i < types.size(); i++) {
            TypeCell cell = types.get(i);
            int x = MARGIN + i * typeCellWidth;
            int center = x + typeCellWidth / 2;
            if (!cell.icons.isEmpty()) {
                graphics.renderItem(cell.icons.get(this.iconIndex % cell.icons.size()), center - 8, TYPE_TOP);
            }
            graphics.drawCenteredString(this.font, typeName(cell.type), center, TYPE_TOP + 18, 0xFFFFFF);
        }

        // Redraw the multiplier boxes above the list widgets (their scrollbars can overlap this row).
        for (TypeCell cell : types) {
            if (cell.box != null) {
                cell.box.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        graphics.drawString(this.font, Component.translatable("guardsgun.config.accuracy.guns"),
                MARGIN, TYPE_TOP + TYPE_HEIGHT + 4, 0xA0A0A0);
        graphics.drawString(this.font, Component.translatable("guardsgun.config.accuracy.selected"),
                MARGIN + panelWidth + GAP, TYPE_TOP + TYPE_HEIGHT + 20, 0xA0A0A0);
        graphics.drawString(this.font, Component.translatable("guardsgun.config.accuracy.multiplier"),
                MARGIN, this.height - 36, 0xA0A0A0);

        if (overrides.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.accuracy.empty"),
                    MARGIN + panelWidth + GAP + panelWidth / 2,
                    TYPE_TOP + TYPE_HEIGHT + 40 + listHeight / 2, 0xA0A0A0);
        }
        if (guns.isEmpty()) {
            int centerX = MARGIN + panelWidth / 2;
            int centerY = TYPE_TOP + TYPE_HEIGHT + 40 + listHeight / 2;
            graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.accuracy.noGuns"),
                    centerX, centerY - 6, 0xFFAA00);
            graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.accuracy.noGuns2"),
                    centerX, centerY + 6, 0xFFAA00);
        }
    }

    private static Component typeName(GunType type) {
        return Component.translatable("guardsgun.config.accuracy.type." + type.getTaCzName());
    }

    private void updateFilter(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        filtered.clear();
        for (GunCatalog.GunInfo gun : guns) {
            if (normalized.isEmpty()
                    || gun.id().toString().toLowerCase(Locale.ROOT).contains(normalized)
                    || gun.name().getString().toLowerCase(Locale.ROOT).contains(normalized)) {
                filtered.add(gun);
            }
        }
        if (gunList != null) {
            gunList.setGuns(filtered);
        }
        selectedGun = null;
        updateButtons();
    }

    private void selectGun(GunCatalog.GunInfo gun) {
        selectedGun = gun;
        selectedOverride = null;
        if (overrideList != null) {
            overrideList.setSelected(null);
        }
        double multiplier = 1.0;
        for (GunOverride override : overrides) {
            if (override.id.equals(gun.id())) {
                multiplier = override.multiplier;
                break;
            }
        }
        multiplierBox.setValue(format(multiplier));
        updateButtons();
    }

    private void selectOverride(GunOverride override) {
        selectedOverride = override;
        selectedGun = null;
        if (gunList != null) {
            gunList.setSelected(null);
        }
        multiplierBox.setValue(format(override.multiplier));
        updateButtons();
    }

    private void addOrUpdate() {
        double multiplier = multiplierBoxValue();
        if (selectedOverride != null) {
            selectedOverride.multiplier = multiplier;
            overrideList.setOverrides(overrides);
            overrideList.setSelected(null);
            selectedOverride = null;
            updateButtons();
            return;
        }
        if (selectedGun == null) {
            return;
        }
        GunOverride existing = null;
        for (GunOverride override : overrides) {
            if (override.id.equals(selectedGun.id())) {
                existing = override;
                break;
            }
        }
        if (existing != null) {
            existing.multiplier = multiplier;
        } else {
            overrides.add(new GunOverride(selectedGun.id(), multiplier, selectedGun.icon(), selectedGun.name()));
        }
        overrideList.setOverrides(overrides);
        overrideList.setSelected(null);
        updateButtons();
    }

    private void removeSelected() {
        if (selectedOverride == null) {
            return;
        }
        overrides.remove(selectedOverride);
        selectedOverride = null;
        overrideList.setOverrides(overrides);
        overrideList.setSelected(null);
        updateButtons();
    }

    private double multiplierBoxValue() {
        try {
            double value = Double.parseDouble(multiplierBox.getValue().trim());
            return Double.isFinite(value) && value >= 0 ? value : 1.0;
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    private static Double parseMultiplier(String text) {
        if (text.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) && value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String format(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }
        return Double.toString(value);
    }

    private void updateButtons() {
        if (addButton == null || removeButton == null) {
            return;
        }
        addButton.active = selectedGun != null || selectedOverride != null;
        removeButton.active = selectedOverride != null;
        addButton.setMessage(Component.translatable(selectedOverride != null
                ? "guardsgun.config.accuracy.update"
                : "guardsgun.config.accuracy.add"));
    }

    private void applyAndClose() {
        List<String> result = new ArrayList<>();
        for (TypeCell cell : types) {
            Double value = parseMultiplier(cell.box.getValue().trim());
            if (value != null) {
                result.add(cell.type.getTaCzName() + "=" + format(value));
            }
        }
        result.addAll(unknownTypeEntries);
        for (GunOverride override : overrides) {
            result.add(override.id + "=" + format(override.multiplier));
        }
        onApply.accept(result);
        onClose();
    }

    private void renderDimBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xFF101010);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static final class TypeCell {
        private final GunType type;
        private final List<ItemStack> icons;
        private EditBox box;
        private String initialValue = "";

        private TypeCell(GunType type, List<ItemStack> icons) {
            this.type = type;
            this.icons = icons;
        }
    }

    private static final class GunOverride {
        private final ResourceLocation id;
        private final ItemStack icon;
        private final Component name;
        private double multiplier;

        private GunOverride(ResourceLocation id, double multiplier, ItemStack icon, Component name) {
            this.id = id;
            this.multiplier = multiplier;
            this.icon = icon;
            this.name = name;
        }
    }

    private final class GunListWidget extends PagedList<GunRow> {
        private GunListWidget(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
        }

        private void setGuns(List<GunCatalog.GunInfo> list) {
            clearEntries();
            for (GunCatalog.GunInfo option : list) {
                addEntry(new GunRow(option));
            }
        }
    }

    private final class OverrideListWidget extends PagedList<OverrideRow> {
        private OverrideListWidget(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
        }

        private void setOverrides(List<GunOverride> list) {
            clearEntries();
            for (GunOverride override : list) {
                addEntry(new OverrideRow(override));
            }
        }
    }

    private final class GunRow extends ObjectSelectionList.Entry<GunRow> {
        private final GunCatalog.GunInfo option;

        private GunRow(GunCatalog.GunInfo option) {
            this.option = option;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (hovering || gunList.getSelected() == this) {
                graphics.fill(left, top, left + width, top + height, 0x40FFFFFF);
            }
            graphics.renderItem(option.icon(), left, top + 3);
            graphics.drawString(AccuracyOverridesScreen.this.font, option.name(), left + 22, top + 7, 0xFFFFFF);
        }

        @Override
        public @NotNull Component getNarration() {
            return option.name();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            gunList.setSelected(this);
            selectGun(option);
            return true;
        }
    }

    private final class OverrideRow extends ObjectSelectionList.Entry<OverrideRow> {
        private final GunOverride override;

        private OverrideRow(GunOverride override) {
            this.override = override;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (hovering || overrideList.getSelected() == this) {
                graphics.fill(left, top, left + width, top + height, 0x40FFFFFF);
            }
            graphics.renderItem(override.icon, left, top + 3);
            graphics.drawString(AccuracyOverridesScreen.this.font, override.name, left + 22, top + 7, 0xFFFFFF);
            String multiplier = "x" + format(override.multiplier);
            graphics.drawString(AccuracyOverridesScreen.this.font, multiplier,
                    left + width - AccuracyOverridesScreen.this.font.width(multiplier) - 6, top + 7, 0xA0A0A0);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(override.name.getString() + " x" + format(override.multiplier));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            overrideList.setSelected(this);
            selectOverride(override);
            return true;
        }
    }
}
