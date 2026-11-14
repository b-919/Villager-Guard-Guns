package com.b919.guardsgun;

import com.b919.guardsgun.integration.GuardItemHandler;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.common.entities.Guard;

import java.util.function.Supplier;

@Mod(Guardsgun.MODID)
public class Guardsgun {
    public static final String MODID = "guardsgun";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<Boolean>> PENDING_GUN =
            ATTACHMENT_TYPES.register("pending_gun", () -> AttachmentType.builder(() -> false).build());

    public static final Supplier<AttachmentType<Boolean>> HAS_GUN_GOAL =
            ATTACHMENT_TYPES.register("has_gun_goal", () -> AttachmentType.builder(() -> false).build());

    public static final Supplier<AttachmentType<Boolean>> GUN_PROCESSED =
            ATTACHMENT_TYPES.register("gun_processed", () ->
                    AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

    public static final Supplier<AttachmentType<Integer>> GUN_ARM_TRIES =
            ATTACHMENT_TYPES.register("gun_arm_tries", () ->
                    AttachmentType.builder(() -> 0).build());

    public static final Supplier<AttachmentType<Integer>> GUN_VERIFY_TICKS =
            ATTACHMENT_TYPES.register("gun_verify_ticks", () ->
                    AttachmentType.builder(() -> 0).build());

    public Guardsgun(IEventBus modEventBus, ModContainer modContainer) {
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        LOGGER.info("GuardsGun initialized.");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                Capabilities.ItemHandler.ENTITY,
                GuardEntityType.GUARD.get(),
                (Guard guard, Void context) -> new GuardItemHandler(guard));
    }
}
