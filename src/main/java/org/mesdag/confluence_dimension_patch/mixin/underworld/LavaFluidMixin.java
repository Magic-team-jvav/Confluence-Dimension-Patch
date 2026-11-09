package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.LavaFluid;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Lava is intentionally sluggish in vanilla (30 ticks per spread). Make it flow noticeably faster
/// only inside the underworld layer, leaving surface lava and every other dimension untouched.
@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
    @ModifyExpressionValue(method = "getSpreadDelay", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/LavaFluid;getTickDelay(Lnet/minecraft/world/level/LevelReader;)I"))
    private int cdp$fasterLavaInUnderworld(int original, Level level, BlockPos pos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && level.dimension().equals(OtherWorld.LEVEL)
        ) return 10;
        return original;
    }
}
