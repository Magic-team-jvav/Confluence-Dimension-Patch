package org.mesdag.confluence_dimension_patch.common;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CDPCommonConfigs {
    public static ModConfigSpec.BooleanValue INITIAL_TERRARIUM;

    public static void register(ModContainer container) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        INITIAL_TERRARIUM = builder.define("initialTerrarium", true);
        container.registerConfig(ModConfig.Type.COMMON, builder.build());
    }
}
