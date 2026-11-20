package com.b919.guardsgun.client;

import com.b919.guardsgun.AmmoMode;
import com.b919.guardsgun.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.Locale;

/** Cloth Config based screen. Cloth is optional; this class is only loaded when it is installed. */
public final class GuardsgunClothConfigScreen {
    private GuardsgunClothConfigScreen() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> create(parent));
    }

    static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(lang("title"))
                .setSavingRunnable(Config::save);

        if (!Config.SPEC.isLoaded()) {
            builder.setEditable(false);
            ConfigEntryBuilder entries = builder.entryBuilder();
            builder.getOrCreateCategory(lang("category.general")).addEntry(
                    entries.startTextDescription(lang("notice.notLoaded"))
                            .setColor(0xFFAA00)
                            .build());
            return builder.build();
        }

        ConfigEntryBuilder entries = builder.entryBuilder();

        buildGeneral(builder, entries);
        buildSpawning(builder, entries);
        buildAmmo(builder, entries);
        buildAccuracy(builder, entries);
        buildReset(builder, entries, parent);

        return builder.build();
    }

    private static void buildReset(ConfigBuilder builder, ConfigEntryBuilder entries, Screen parent) {
        ConfigCategory reset = builder.getOrCreateCategory(lang("category.reset"));
        reset.addEntry(entries.startTextDescription(lang("reset.warning"))
                .setColor(0xFFAA00)
                .build());
        reset.addEntry(new ResetConfigEntry(lang("reset.button"), parent));
    }

    private static void buildGeneral(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory general = builder.getOrCreateCategory(lang("category.general"));
        if (Minecraft.getInstance().getCurrentServer() != null) {
            general.addEntry(entries.startTextDescription(lang("notice.remoteServer"))
                    .setColor(0xFFAA00)
                    .build());
        }

        general.addEntry(entries.startBooleanToggle(lang("enableGuns"), Config.ENABLE_GUNS.get())
                .setDefaultValue(Config.ENABLE_GUNS.getDefault())
                .setTooltip(tooltip("enableGuns"))
                .setSaveConsumer(Config.ENABLE_GUNS::set)
                .build());

        general.addEntry(entries.startBooleanToggle(lang("friendlyFire"), Config.FRIENDLY_FIRE.get())
                .setDefaultValue(Config.FRIENDLY_FIRE.getDefault())
                .setTooltip(tooltip("friendlyFire"))
                .setSaveConsumer(Config.FRIENDLY_FIRE::set)
                .build());
    }

    private static void buildSpawning(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory spawning = builder.getOrCreateCategory(lang("category.spawning"));

        spawning.addEntry(entries.startIntSlider(lang("gunSpawnChance"), Config.GUN_SPAWN_CHANCE.get(), 0, 100)
                .setDefaultValue(Config.GUN_SPAWN_CHANCE.getDefault())
                .setTextGetter(value -> Component.translatable("guardsgun.config.percent", value))
                .setTooltip(tooltip("gunSpawnChance"))
                .setSaveConsumer(Config.GUN_SPAWN_CHANCE::set)
                .build());

        spawning.addEntry(new WeaponPoolEntry(
                lang("weaponWeights"),
                new ArrayList<>(Config.WEAPON_WEIGHTS.get()),
                new ArrayList<>(Config.WEAPON_WEIGHTS.getDefault()),
                Config.WEAPON_WEIGHTS::set));
    }

    private static void buildAmmo(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory ammo = builder.getOrCreateCategory(lang("category.ammo"));

        ammo.addEntry(entries.startEnumSelector(lang("ammoMode"), AmmoMode.class,
                        AmmoMode.fromString(Config.AMMO_MODE.get()))
                .setDefaultValue(AmmoMode.fromString(Config.AMMO_MODE.getDefault()))
                .setEnumNameProvider(value -> lang("ammoMode." + value.name().toLowerCase(Locale.ROOT)))
                .setTooltip(tooltip("ammoMode"))
                .setSaveConsumer(mode -> Config.AMMO_MODE.set(mode.name()))
                .build());

        ammo.addEntry(entries.startIntSlider(lang("ammoFillPercent"), Config.AMMO_FILL_PERCENT.get(), 0, 100)
                .setDefaultValue(Config.AMMO_FILL_PERCENT.getDefault())
                .setTextGetter(value -> Component.translatable("guardsgun.config.percent", value))
                .setTooltip(tooltip("ammoFillPercent"))
                .setSaveConsumer(Config.AMMO_FILL_PERCENT::set)
                .build());

        ammo.addEntry(entries.startIntSlider(lang("ammoBoxLevel"), Config.AMMO_BOX_LEVEL.get(), 0, 2)
                .setDefaultValue(Config.AMMO_BOX_LEVEL.getDefault())
                .setTextGetter(value -> lang("ammoBoxLevel." + value))
                .setTooltip(tooltip("ammoBoxLevel"))
                .setSaveConsumer(Config.AMMO_BOX_LEVEL::set)
                .build());

        ammo.addEntry(entries.startBooleanToggle(lang("refillAmmo"), Config.REFILL_AMMO.get())
                .setDefaultValue(Config.REFILL_AMMO.getDefault())
                .setTooltip(tooltip("refillAmmo"))
                .setSaveConsumer(Config.REFILL_AMMO::set)
                .build());

        ammo.addEntry(entries.startIntSlider(lang("refillThresholdPercent"), Config.REFILL_THRESHOLD_PERCENT.get(), 0, 100)
                .setDefaultValue(Config.REFILL_THRESHOLD_PERCENT.getDefault())
                .setTextGetter(value -> Component.translatable("guardsgun.config.percent", value))
                .setTooltip(tooltip("refillThresholdPercent"))
                .setSaveConsumer(Config.REFILL_THRESHOLD_PERCENT::set)
                .build());

        ammo.addEntry(entries.startBooleanToggle(lang("refillAtNight"), Config.REFILL_AT_NIGHT.get())
                .setDefaultValue(Config.REFILL_AT_NIGHT.getDefault())
                .setTooltip(tooltip("refillAtNight"))
                .setSaveConsumer(Config.REFILL_AT_NIGHT::set)
                .build());

        ammo.addEntry(entries.startIntField(lang("refillCooldownSeconds"), Config.REFILL_COOLDOWN_SECONDS.get())
                .setDefaultValue(Config.REFILL_COOLDOWN_SECONDS.getDefault())
                .setMin(1)
                .setMax(3600)
                .setTooltip(tooltip("refillCooldownSeconds"))
                .setSaveConsumer(Config.REFILL_COOLDOWN_SECONDS::set)
                .build());
    }

    private static void buildAccuracy(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory accuracy = builder.getOrCreateCategory(lang("category.accuracy"));

        accuracy.addEntry(entries.startDoubleField(lang("defaultAccuracyMultiplier"), Config.DEFAULT_ACCURACY_MULTIPLIER.get())
                .setDefaultValue(Config.DEFAULT_ACCURACY_MULTIPLIER.getDefault())
                .setMin(0.0)
                .setMax(100.0)
                .setTooltip(tooltip("defaultAccuracyMultiplier"))
                .setSaveConsumer(Config.DEFAULT_ACCURACY_MULTIPLIER::set)
                .build());

        accuracy.addEntry(new AccuracyOverridesEntry(
                lang("accuracyOverrides"),
                new ArrayList<>(Config.ACCURACY_OVERRIDES.get()),
                new ArrayList<>(Config.ACCURACY_OVERRIDES.getDefault()),
                Config.ACCURACY_OVERRIDES::set));
    }

    private static Component lang(String key) {
        return Component.translatable("guardsgun.config." + key);
    }

    private static Component[] tooltip(String key) {
        return new Component[]{Component.translatable("guardsgun.config." + key + ".tooltip")
                .withStyle(ChatFormatting.GRAY)};
    }
}
