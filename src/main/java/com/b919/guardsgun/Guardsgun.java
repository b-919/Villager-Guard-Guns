package com.b919.guardsgun;

import com.b919.guardsgun.client.ClientConfigSetup;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Guardsgun.MODID)
public class Guardsgun {
    public static final String MODID = "guardsgun";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Guardsgun() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientConfigSetup.register();
        }
        LOGGER.info("GuardsGun initialized.");
    }
}
