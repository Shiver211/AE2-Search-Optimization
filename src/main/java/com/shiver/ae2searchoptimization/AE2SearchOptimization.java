package com.shiver.ae2searchoptimization;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:appliedenergistics2;required-after:mixinbooter"
)
public final class AE2SearchOptimization {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        AE2SearchOptimizationConfig.load(event.getSuggestedConfigurationFile());
        LOGGER.info("Loaded {}", Tags.MOD_NAME);
    }
}
