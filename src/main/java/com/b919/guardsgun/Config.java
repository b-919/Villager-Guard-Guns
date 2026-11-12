package com.b919.guardsgun;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventBusSubscriber(modid = Guardsgun.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_GUNS = BUILDER
            .comment("when false the full mod gets disabled.")
            .define("enableGuns", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> WEAPON_WEIGHTS = BUILDER
            .comment(
                    "Weighted pool of TaCZ guns that guards can spawn with.",
                    "Each entry is '<modid:gun_id>=<weight>'. A guard's chance to spawn with a gun",
                    "it works by '<weight> / <sum of all weights>'. So higher weight = more common.",
                    "Leave empty to disable gun spawning.",
                    "Example: [\"tacz:ak47=50\", \"tacz:m4a1=30\", \"tacz:deagle=20\"]")
            .defineListAllowEmpty("weaponWeights", List.of("tacz:ak47=50", "tacz:m4a1=30", "tacz:deagle=20"), Config::validateWeaponEntry);

    private static final ModConfigSpec.ConfigValue<String> AMMO_MODE = BUILDER
            .comment(
                    "How guards use ammo:",
                    "'INFINITEAMMO' - the magazine never runs out.",
                    "'INFINITEMAGS' - the magazine runs out and the guard reloads, but the reserve ammo never depletes (like a creative ammo box).",
                    "'NORMAL' - the magazine runs out and the guard reloads, but it consumes the reserve ammo.")
            .define("ammoMode", "NORMAL");

    private static final ModConfigSpec.IntValue AMMO_FILL_PERCENT = BUILDER
            .comment(
                    "How full the guard's ammo box is when it spawns, measured by fill %.",
                    "100 = the box is completely filled with ammo.",
                    "0 = the box is empty.",
                    "Only used when ammoMode is NORMAL.")
            .defineInRange("ammoFillPercent", 100, 0, 100);

    private static final ModConfigSpec.IntValue AMMO_BOX_LEVEL = BUILDER
            .comment(
                    "The ammo box tier guards spawn with: 0 = iron, 1 = gold, 2 = diamond.",
                    "Higher tiers hold more ammo. Ignored when ammoMode is INFINITEMAGS (which uses a creative box).")
            .defineInRange("ammoBoxLevel", 0, 0, 2);

    private static final ModConfigSpec.BooleanValue REFILL_AMMO = BUILDER
            .comment(
                    "When true, guards will go to a nearby TaCZ ammo storage to refill",
                    "They will also exchange the ammo of a box holding the wrong ammo type for their gun")
            .define("refillAmmo", true);

    private static final ModConfigSpec.IntValue REFILL_THRESHOLD_PERCENT = BUILDER
            .comment(
                    "A guard starts looking for ammo storage when its ammo box is at or below this",
                    "percentage of the box's capacity. 0 = only refill a completely empty box.",
                    "Only used when ammoMode is NORMAL.")
            .defineInRange("refillThresholdPercent", 20, 0, 100);

    private static final ModConfigSpec.BooleanValue REFILL_AT_NIGHT = BUILDER
            .comment(
                    "When true, guards may also refill and exchange ammo at night.",
                    "When false, they only do so during the day.")
            .define("refillAtNight", true);

    private static final ModConfigSpec.IntValue REFILL_COOLDOWN_SECONDS = BUILDER
            .comment(
                    "How long a guard waits, in seconds, before scanning nearby ammo storages again",
                    "when none of them can serve its current need.",
                    "Measured in real seconds: 20 game ticks = 1 second.")
            .defineInRange("refillCooldownSeconds", 60, 1, 3600);

    private static final ModConfigSpec.DoubleValue DEFAULT_ACCURACY_MULTIPLIER = BUILDER
            .comment(
                    "Default inaccuracy multiplier for every gun type not listed in accuracyOverrides.",
                    "1.0 = normal aim, values above 1.0 = worse aim, values below 1.0 = better aim.")
            .defineInRange("defaultAccuracyMultiplier", 1.0, 0.0, 100.0);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ACCURACY_OVERRIDES = BUILDER
            .comment(
                    "Per-gun-type inaccuracy multiplier. Each entry is '<type>=<multiplier>'.",
                    "Known TaCZ types: rifle, smg, pistol, sniper, shotgun, mg, rpg.",
                    "1.0 = normal aim, > 1.0 = worse aim, < 1.0 = better aim",
                    "Example: [\"sniper=0.4\", \"pistol=1.5\"]")
            .defineListAllowEmpty("accuracyOverrides", List.of("sniper=0.4", "pistol=2.0", "rifle=1.0", "smg=0.7", "shotgun=0.2", "mg=0.6", "rpg=0.6"), Config::validateAccuracyEntry);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static final int TICKS_PER_SECOND = 20;

    public static boolean enableGuns;
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

    private static boolean validateWeaponEntry(final Object obj) {
        if (!(obj instanceof String s)) {
            return false;
        }
        String[] parts = s.split("=", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            ResourceLocation.parse(parts[0].trim());
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
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        enableGuns = ENABLE_GUNS.get();
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
            String[] parts = entry.split("=", 2);
            pool.add(new WeaponEntry(ResourceLocation.parse(parts[0].trim()), Integer.parseInt(parts[1].trim())));
        }
        weaponPool = List.copyOf(pool);

        Map<String, Double> overrides = new HashMap<>();
        for (String entry : ACCURACY_OVERRIDES.get()) {
            String[] parts = entry.split("=", 2);
            overrides.put(parts[0].trim().toLowerCase(Locale.ROOT), Double.parseDouble(parts[1].trim()));
        }
        accuracyOverrides = Map.copyOf(overrides);
    }

    private Config() {
    }
}
