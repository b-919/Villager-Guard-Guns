package com.b919.guardsgun.client;

import net.minecraftforge.fml.ModList;

/**
 * Client-only entry point for registering the config screen.
 * Kept separate so no client or Cloth Config classes are touched on a dedicated server.
 */
public final class ClientConfigSetup {
    private static final String CLOTH_CONFIG_MODID = "cloth_config";

    private ClientConfigSetup() {
    }

    public static void register() {
        if (ModList.get().isLoaded(CLOTH_CONFIG_MODID)) {
            GuardsgunClothConfigScreen.register();
        } else {
            MissingClothConfigScreen.register();
        }
    }
}
