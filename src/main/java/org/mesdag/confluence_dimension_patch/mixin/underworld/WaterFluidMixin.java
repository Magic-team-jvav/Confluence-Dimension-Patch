package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.WaterFluid;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Keeps water from the surface layer out of the underworld. Water is allowed right down to the
/// boundary, but it can never replace a fluid below it.
@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin {
    @ModifyReturnValue(method = "canBeReplacedWith", at = @At("RETURN"))
    private boolean cdp$preventWaterEnteringUnderworld(
            boolean original,
            @Local(argsOnly = true) BlockGetter blockReader,
            @Local(argsOnly = true) BlockPos pos,
            @Local(argsOnly = true) Direction direction
    ) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && direction == Direction.DOWN
                && pos.getY() < OverworldUtils.getCaveY()
                && blockReader instanceof Level level
                && level.dimension().equals(OtherWorld.LEVEL)
        ) return false;
        return original;
    }
}
