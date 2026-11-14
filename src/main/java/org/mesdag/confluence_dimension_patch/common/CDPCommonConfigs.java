package org.mesdag.confluence_dimension_patch.common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class CDPCommonConfigs {
    public static ForgeConfigSpec.BooleanValue INITIAL_TERRARIUM;

    public static void register(FMLJavaModLoadingContext context) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        INITIAL_TERRARIUM = builder.define("initialTerrarium", true);
        context.registerConfig(ModConfig.Type.COMMON, builder.build());
    }
}
