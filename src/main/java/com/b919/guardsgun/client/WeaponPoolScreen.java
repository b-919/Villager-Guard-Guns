package com.b919.guardsgun.client;

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
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Icon picker for the guard weapon pool.*/
public class WeaponPoolScreen extends Screen {
    private static final int MARGIN = 20;
    private static final int GAP = 16;
    private static final int TOP = 50;
    private static final int ROW = 22;
    private final Screen parent;
    private final Consumer<List<String>> onApply;
    private final List<GunCatalog.GunInfo> guns = new ArrayList<>();
    private final List<GunCatalog.GunInfo> filtered = new ArrayList<>();
    private final List<Weapon> pool = new ArrayList<>();
    private int panelWidth;
    private int listHeight;
    private GunListWidget gunList;
    private PoolListWidget poolList;
    private EditBox searchBox;
    private EditBox weightBox;
    private Button addButton;
    private Button removeButton;
    private GunCatalog.GunInfo selectedGun;
    private Weapon selectedWeapon;

    public WeaponPoolScreen(Screen parent, List<String> current, Consumer<List<String>> onApply) {
        super(Component.translatable("guardsgun.config.weaponPool.title"));
        this.parent = parent;
        this.onApply = onApply;
        loadGuns();
        parsePool(current);
    }

    private void loadGuns() {
        guns.addAll(GunCatalog.collect());
        guns.sort(Comparator.comparing(gun -> gun.name().getString(), String.CASE_INSENSITIVE_ORDER));
        filtered.addAll(guns);
    }

    private void parsePool(List<String> entries) {
        for (String entry : entries) {
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            ResourceLocation id;
            try {
                id = ResourceLocation.parse(entry.substring(0, separator).trim());
            } catch (Exception e) {
                continue;
            }
            int weight;
            try {
                weight = Math.max(0, Integer.parseInt(entry.substring(separator + 1).trim()));
            } catch (NumberFormatException e) {
                continue;
            }
            pool.add(createWeapon(id, weight));
        }
    }

    private Weapon createWeapon(ResourceLocation id, int weight) {
        for (GunCatalog.GunInfo gun : guns) {
            if (gun.id().equals(id)) {
                return new Weapon(id, weight, gun.icon(), gun.name());
            }
        }
        return new Weapon(id, weight, new ItemStack(Items.BARRIER), Component.literal(id.toString()));
    }

    @Override
    protected void init() {
        panelWidth = Math.max(120, (this.width - MARGIN * 2 - GAP) / 2);
        listHeight = Math.max(40, this.height - TOP - 58);

        searchBox = new EditBox(this.font, MARGIN, TOP - 24, panelWidth, 18,
                Component.translatable("guardsgun.config.weaponPool.search"));
        searchBox.setResponder(this::updateFilter);
        addRenderableWidget(searchBox);

        gunList = new GunListWidget(this.minecraft, panelWidth, listHeight, TOP, ROW);
        gunList.setX(MARGIN);
        gunList.setY(TOP);
        addRenderableWidget(gunList);
        gunList.setGuns(filtered);

        poolList = new PoolListWidget(this.minecraft, panelWidth, listHeight, TOP, ROW);
        poolList.setX(MARGIN + panelWidth + GAP);
        poolList.setY(TOP);
        addRenderableWidget(poolList);
        poolList.setWeapons(pool);

        int barY = this.height - 50;
        weightBox = new EditBox(this.font, MARGIN + 56, barY, 56, 20,
                Component.translatable("guardsgun.config.weaponPool.weight"));
        weightBox.setFilter(value -> value.matches("\\d*"));
        weightBox.setMaxLength(8);
        weightBox.setValue("10");
        addRenderableWidget(weightBox);

        addButton = Button.builder(Component.translatable("guardsgun.config.weaponPool.add"),
                        pressed -> addOrUpdate())
                .bounds(MARGIN + 120, barY, 80, 20)
                .build();
        addRenderableWidget(addButton);

        removeButton = Button.builder(Component.translatable("guardsgun.config.weaponPool.remove"),
                        pressed -> removeSelected())
                .bounds(MARGIN + 208, barY, 80, 20)
                .build();
        addRenderableWidget(removeButton);

        addRenderableWidget(Button.builder(Component.translatable("guardsgun.config.weaponPool.apply"),
                        pressed -> applyAndClose())
                .bounds(this.width / 2 - 104, this.height - 24, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), pressed -> onClose())
                .bounds(this.width / 2 + 4, this.height - 24, 100, 20)
                .build());

        updateButtons();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("guardsgun.config.weaponPool.weight"),
                MARGIN, this.height - 44, 0xA0A0A0);
        graphics.drawString(this.font, Component.translatable("guardsgun.config.weaponPool.selected"),
                MARGIN + panelWidth + GAP, TOP - 14, 0xA0A0A0);

        if (pool.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.weaponPool.empty"),
                    MARGIN + panelWidth + GAP + panelWidth / 2, TOP + listHeight / 2, 0xA0A0A0);
        }
        if (guns.isEmpty()) {
            int centerX = MARGIN + panelWidth / 2;
            int centerY = TOP + listHeight / 2;
            graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.weaponPool.noGuns"),
                    centerX, centerY - 6, 0xFFAA00);
            graphics.drawCenteredString(this.font, Component.translatable("guardsgun.config.weaponPool.noGuns2"),
                    centerX, centerY + 6, 0xFFAA00);
        }
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
        selectedWeapon = null;
        if (poolList != null) {
            poolList.setSelected(null);
        }
        int weight = 10;
        for (Weapon weapon : pool) {
            if (weapon.id.equals(gun.id())) {
                weight = weapon.weight;
                break;
            }
        }
        weightBox.setValue(Integer.toString(weight));
        updateButtons();
    }

    private void selectWeapon(Weapon weapon) {
        selectedWeapon = weapon;
        selectedGun = null;
        if (gunList != null) {
            gunList.setSelected(null);
        }
        weightBox.setValue(Integer.toString(weapon.weight));
        updateButtons();
    }

    private void addOrUpdate() {
        int weight = parseWeight();
        if (selectedWeapon != null) {
            selectedWeapon.weight = weight;
            poolList.setWeapons(pool);
            poolList.setSelected(null);
            selectedWeapon = null;
            updateButtons();
            return;
        }
        if (selectedGun == null) {
            return;
        }
        Weapon existing = null;
        for (Weapon weapon : pool) {
            if (weapon.id.equals(selectedGun.id())) {
                existing = weapon;
                break;
            }
        }
        if (existing != null) {
            existing.weight = weight;
        } else {
            pool.add(new Weapon(selectedGun.id(), weight, selectedGun.icon(), selectedGun.name()));
        }
        poolList.setWeapons(pool);
        poolList.setSelected(null);
        updateButtons();
    }

    private void removeSelected() {
        if (selectedWeapon == null) {
            return;
        }
        pool.remove(selectedWeapon);
        selectedWeapon = null;
        poolList.setWeapons(pool);
        poolList.setSelected(null);
        updateButtons();
    }

    private int parseWeight() {
        try {
            return Math.max(0, Integer.parseInt(weightBox.getValue().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateButtons() {
        if (addButton == null || removeButton == null) {
            return;
        }
        addButton.active = selectedGun != null || selectedWeapon != null;
        removeButton.active = selectedWeapon != null;
        addButton.setMessage(Component.translatable(selectedWeapon != null
                ? "guardsgun.config.weaponPool.update"
                : "guardsgun.config.weaponPool.add"));
    }

    private void applyAndClose() {
        List<String> result = new ArrayList<>(pool.size());
        for (Weapon weapon : pool) {
            result.add(weapon.id + "=" + weapon.weight);
        }
        onApply.accept(result);
        onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static final class Weapon {
        private final ResourceLocation id;
        private final ItemStack icon;
        private final Component name;
        private int weight;

        private Weapon(ResourceLocation id, int weight, ItemStack icon, Component name) {
            this.id = id;
            this.weight = weight;
            this.icon = icon;
            this.name = name;
        }
    }

    private final class GunListWidget extends PagedList<GunRow> {
        private GunListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void setGuns(List<GunCatalog.GunInfo> list) {
            clearEntries();
            for (GunCatalog.GunInfo option : list) {
                addEntry(new GunRow(option));
            }
        }
    }

    private final class PoolListWidget extends PagedList<PoolRow> {
        private PoolListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void setWeapons(List<Weapon> list) {
            clearEntries();
            for (Weapon weapon : list) {
                addEntry(new PoolRow(weapon));
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
            graphics.drawString(WeaponPoolScreen.this.font, option.name(), left + 22, top + 7, 0xFFFFFF);
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

    private final class PoolRow extends ObjectSelectionList.Entry<PoolRow> {
        private final Weapon weapon;

        private PoolRow(Weapon weapon) {
            this.weapon = weapon;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (hovering || poolList.getSelected() == this) {
                graphics.fill(left, top, left + width, top + height, 0x40FFFFFF);
            }
            graphics.renderItem(weapon.icon, left, top + 3);
            graphics.drawString(WeaponPoolScreen.this.font, weapon.name, left + 22, top + 7, 0xFFFFFF);
            String weight = "x" + weapon.weight;
            graphics.drawString(WeaponPoolScreen.this.font, weight,
                    left + width - WeaponPoolScreen.this.font.width(weight) - 6, top + 7, 0xA0A0A0);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(weapon.name.getString() + " x" + weapon.weight);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            poolList.setSelected(this);
            selectWeapon(weapon);
            return true;
        }
    }
}
