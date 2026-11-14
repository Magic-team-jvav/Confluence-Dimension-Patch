package org.mesdag.confluence_dimension_patch;

import org.mesdag.portlib.config.PortConfigSpec;

public class CDPStartupConfigs {
    private static PortConfigSpec.BooleanValue ENABLE_TERRARIA_LIKE_WORLD_GENERATION;

    static void register() {
        PortConfigSpec.Builder builder = PortConfigSpec.builder(ConfluenceDimensionPatch.MODID);
        ENABLE_TERRARIA_LIKE_WORLD_GENERATION = builder.define("enableTerrariaLikeWorldGeneration", true);
        PortConfigSpec spec = builder.build();
        spec.load();
    }

    public static boolean enableTerrariaLikeWorldGeneration() {
        return ENABLE_TERRARIA_LIKE_WORLD_GENERATION != null && ENABLE_TERRARIA_LIKE_WORLD_GENERATION.get();
    }
}
