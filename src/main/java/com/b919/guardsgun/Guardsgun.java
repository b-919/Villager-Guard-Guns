package com.b919.guardsgun;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Guardsgun.MODID)
public class Guardsgun {
    public static final String MODID = "guardsgun";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Guardsgun() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        LOGGER.info("GuardsGun initialized.");
    }
}
