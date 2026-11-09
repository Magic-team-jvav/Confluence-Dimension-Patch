package org.mesdag.confluence_dimension_patch;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CDPStartupConfigs {
    private static ModConfigSpec.BooleanValue ENABLE_TERRARIA_LIKE_WORLD_GENERATION;

    static void register(ModContainer container) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_TERRARIA_LIKE_WORLD_GENERATION = builder.define("enableTerrariaLikeWorldGeneration", false);
        container.registerConfig(ModConfig.Type.STARTUP, builder.build());
    }

    public static boolean enableTerrariaLikeWorldGeneration() {
        return ENABLE_TERRARIA_LIKE_WORLD_GENERATION != null && ENABLE_TERRARIA_LIKE_WORLD_GENERATION.get();
    }
}
