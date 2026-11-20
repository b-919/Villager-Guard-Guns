package com.b919.guardsgun;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Guardsgun.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_GUNS = BUILDER
            .comment("when false the full mod gets disabled.")
            .define("enableGuns", true);

    public static final ForgeConfigSpec.BooleanValue FRIENDLY_FIRE = BUILDER
            .comment(
                    "When true, guards cannot damage the mobs listed in Guard Villagers'",
                    "'Mobs that guards actively protect when they get hurt' setting, plus team allies,",
                    "their owner, and players with Hero of the Village. This mainly prevents stray gunshots deaths.",
                    "Set to false to allow friendly fire.")
            .define("friendlyFire", true);

    public static final ForgeConfigSpec.IntValue GUN_SPAWN_CHANCE = BUILDER
            .comment(
                    "Chance, in percent, that a guard spawns with a gun.",
                    "100 = every guard gets a gun (default). 0 = guards never spawn with guns.")
            .defineInRange("gunSpawnChance", 100, 0, 100);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WEAPON_WEIGHTS = BUILDER
            .comment(
                    "Weighted pool of TaCZ guns that guards can spawn with.",
                    "Each entry is '<modid:gun_id>=<weight>'. A guard's chance to spawn with a gun",
                    "it works by '<weight> / <sum of all weights>'. So higher weight = more common.",
                    "Leave empty to disable gun spawning.",
                    "Example: [\"tacz:ak47=50\", \"tacz:m4a1=30\", \"tacz:deagle=20\"]")
            .defineListAllowEmpty("weaponWeights", List.of("tacz:ak47=30", "tacz:svd=10", "tacz:rpg7=5", "tacz:type_81=30", "tacz:m1897=15", "tacz:m1911=30", "tacz:deagle=15"), Config::validateWeaponEntry);

    public static final ForgeConfigSpec.ConfigValue<String> AMMO_MODE = BUILDER
            .comment(
                    "How guards use ammo:",
                    "'INFINITEAMMO' - the magazine never runs out.",
                    "'INFINITEMAGS' - the magazine runs out and the guard reloads, but the reserve ammo never depletes (like a creative ammo box).",
                    "'NORMAL' - the magazine runs out and the guard reloads, but it consumes the reserve ammo.")
            .define("ammoMode", "NORMAL");

    public static final ForgeConfigSpec.IntValue AMMO_FILL_PERCENT = BUILDER
            .comment(
                    "How full the guard's ammo box is when it spawns, measured by fill %.",
                    "100 = the box is completely filled with ammo.",
                    "0 = the box is empty.",
                    "Only used when ammoMode is NORMAL.")
            .defineInRange("ammoFillPercent", 100, 0, 100);

    public static final ForgeConfigSpec.IntValue AMMO_BOX_LEVEL = BUILDER
            .comment(
                    "The ammo box tier guards spawn with: 0 = iron, 1 = gold, 2 = diamond.",
                    "Higher tiers hold more ammo. Ignored when ammoMode is INFINITEMAGS (which uses a creative box).")
            .defineInRange("ammoBoxLevel", 0, 0, 2);

    public static final ForgeConfigSpec.BooleanValue REFILL_AMMO = BUILDER
            .comment(
                    "When true, guards will go to a nearby TaCZ ammo storage to refill",
                    "They will also exchange the ammo of a box holding the wrong ammo type for their gun")
            .define("refillAmmo", true);

    public static final ForgeConfigSpec.IntValue REFILL_THRESHOLD_PERCENT = BUILDER
            .comment(
                    "A guard starts looking for ammo storage when its ammo box is at or below this",
                    "percentage of the box's capacity. 0 = only refill a completely empty box.",
                    "Only used when ammoMode is NORMAL.")
            .defineInRange("refillThresholdPercent", 20, 0, 100);

    public static final ForgeConfigSpec.BooleanValue REFILL_AT_NIGHT = BUILDER
            .comment(
                    "When true, guards may also refill and exchange ammo at night.",
                    "When false, they only do so during the day.")
            .define("refillAtNight", true);

    public static final ForgeConfigSpec.IntValue REFILL_COOLDOWN_SECONDS = BUILDER
            .comment(
                    "How long a guard waits, in seconds, before scanning nearby ammo storages again",
                    "when none of them can serve its current need.",
                    "Measured in real seconds: 20 game ticks = 1 second.")
            .defineInRange("refillCooldownSeconds", 60, 1, 3600);

    public static final ForgeConfigSpec.DoubleValue DEFAULT_ACCURACY_MULTIPLIER = BUILDER
            .comment(
                    "Default inaccuracy multiplier for every gun type not listed in accuracyOverrides.",
                    "1.0 = normal aim, values above 1.0 = worse aim, values below 1.0 = better aim.")
            .defineInRange("defaultAccuracyMultiplier", 1.0, 0.0, 100.0);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ACCURACY_OVERRIDES = BUILDER
            .comment(
                    "Inaccuracy multiplier per gun type or per specific gun.",
                    "Each entry is '<type>=<multiplier>' or '<modid:gun_id>=<multiplier>'.",
                    "Per-gun entries take priority over type entries.",
                    "Known TaCZ types: rifle, smg, pistol, sniper, shotgun, mg, rpg, dmr.",
                    "1.0 = normal aim, > 1.0 = worse aim, < 1.0 = better aim",
                    "Example: [\"sniper=0.4\", \"tacz:type_81=0.5\", \"pistol=1.5\"]")
            .defineListAllowEmpty("accuracyOverrides", List.of("sniper=0.4", "dmr=0.6", "pistol=2.0", "rifle=1.0", "smg=0.7", "shotgun=0.2", "mg=0.6", "rpg=0.6"), Config::validateAccuracyEntry);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static final int TICKS_PER_SECOND = 20;

    public static boolean enableGuns;
    public static int gunSpawnChance;
    public static boolean friendlyFire;
    public static AmmoMode ammoMode = AmmoMode.NORMAL;
    public static int ammoFillPercent;
    public static int ammoBoxLevel;
    public static boolean refillAmmo;
    public static int refillThresholdPercent;
    public static boolean refillAtNight;
    public static int refillCooldownTicks;
    public static double defaultAccuracyMultiplier;
    public static List<WeaponEntry> weaponPool = List.of();
    public static Map<String, Double> accuracyOverrides = Map.of();
    public static Map<String, Double> gunAccuracyOverrides = Map.of();

    private static boolean validateWeaponEntry(final Object obj) {
        if (!(obj instanceof String s)) {
            return false;
        }
        String[] parts = s.split("=", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            new ResourceLocation(parts[0].trim());
        } catch (Exception e) {
            return false;
        }
        try {
            return Integer.parseInt(parts[1].trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean validateAccuracyEntry(final Object obj) {
        if (!(obj instanceof String s)) {
            return false;
        }
        String[] parts = s.split("=", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            return Double.parseDouble(parts[1].trim()) >= 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        reload();
    }

    public static void reload() {
        enableGuns = ENABLE_GUNS.get();
        gunSpawnChance = GUN_SPAWN_CHANCE.get();
        friendlyFire = FRIENDLY_FIRE.get();
        ammoMode = AmmoMode.fromString(AMMO_MODE.get());
        ammoFillPercent = AMMO_FILL_PERCENT.get();
        ammoBoxLevel = AMMO_BOX_LEVEL.get();
        refillAmmo = REFILL_AMMO.get();
        refillThresholdPercent = REFILL_THRESHOLD_PERCENT.get();
        refillAtNight = REFILL_AT_NIGHT.get();
        refillCooldownTicks = REFILL_COOLDOWN_SECONDS.get() * TICKS_PER_SECOND;
        defaultAccuracyMultiplier = DEFAULT_ACCURACY_MULTIPLIER.get();

        List<WeaponEntry> pool = new ArrayList<>();
        for (String entry : WEAPON_WEIGHTS.get()) {
            if (!validateWeaponEntry(entry)) {
                continue;
            }
            String[] parts = entry.split("=", 2);
            pool.add(new WeaponEntry(new ResourceLocation(parts[0].trim()), Integer.parseInt(parts[1].trim())));
        }
        weaponPool = List.copyOf(pool);

        Map<String, Double> overrides = new HashMap<>();
        Map<String, Double> gunOverrides = new HashMap<>();
        for (String entry : ACCURACY_OVERRIDES.get()) {
            if (!validateAccuracyEntry(entry)) {
                continue;
            }
            String[] parts = entry.split("=", 2);
            String key = parts[0].trim().toLowerCase(Locale.ROOT);
            double multiplier = Double.parseDouble(parts[1].trim());
            if (key.contains(":")) {
                gunOverrides.put(key, multiplier);
            } else {
                overrides.put(key, multiplier);
            }
        }
        accuracyOverrides = Map.copyOf(overrides);
        gunAccuracyOverrides = Map.copyOf(gunOverrides);
    }

    public static void save() {
        if (!SPEC.isLoaded()) {
            return;
        }
        SPEC.save();
        reload();
    }

    public static void resetToDefaults() {
        ENABLE_GUNS.set(ENABLE_GUNS.getDefault());
        FRIENDLY_FIRE.set(FRIENDLY_FIRE.getDefault());
        GUN_SPAWN_CHANCE.set(GUN_SPAWN_CHANCE.getDefault());
        WEAPON_WEIGHTS.set(new ArrayList<>(WEAPON_WEIGHTS.getDefault()));
        AMMO_MODE.set(AMMO_MODE.getDefault());
        AMMO_FILL_PERCENT.set(AMMO_FILL_PERCENT.getDefault());
        AMMO_BOX_LEVEL.set(AMMO_BOX_LEVEL.getDefault());
        REFILL_AMMO.set(REFILL_AMMO.getDefault());
        REFILL_THRESHOLD_PERCENT.set(REFILL_THRESHOLD_PERCENT.getDefault());
        REFILL_AT_NIGHT.set(REFILL_AT_NIGHT.getDefault());
        REFILL_COOLDOWN_SECONDS.set(REFILL_COOLDOWN_SECONDS.getDefault());
        DEFAULT_ACCURACY_MULTIPLIER.set(DEFAULT_ACCURACY_MULTIPLIER.getDefault());
        ACCURACY_OVERRIDES.set(new ArrayList<>(ACCURACY_OVERRIDES.getDefault()));
        save();
    }

    private Config() {
    }
}
