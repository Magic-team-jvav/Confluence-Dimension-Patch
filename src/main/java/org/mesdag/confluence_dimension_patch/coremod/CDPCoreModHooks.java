package org.mesdag.confluence_dimension_patch.coremod;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;

/// Runtime side of [META-INF/coremods.json], which injects these calls into the head of
/// `IForgeBlock#isFireSource`. Mixin cannot target interfaces, so the transformation is done by the
/// Forge coremod transformer instead (see `coremods/underworld_fire_source.js`).
///
/// The injected bytecode is equivalent to:
/// ```java
/// if (CDPCoreModHooks.isUnderworld(level, pos)) return CDPCoreModHooks.isNetherInfiniburn(state);
/// ```
public final class CDPCoreModHooks {
    private CDPCoreModHooks() {
    }

    /// Whether fire should use the underworld's infini-burn rules at the given position.
    public static boolean isUnderworld(LevelReader level, BlockPos pos) {
        return CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && level instanceof Level actualLevel
                && actualLevel.dimension() == OtherWorld.LEVEL;
    }

    /// In the underworld only nether infini-burn blocks (netherrack and friends) keep fire alive.
    public static boolean isNetherInfiniburn(BlockState state) {
        return state.is(BlockTags.INFINIBURN_NETHER);
    }
}
