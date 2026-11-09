package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.confluence.lib.mixed.SelfGetter;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// [WaterFluidMixin] covers the case where the target position already contains a fluid.
/// This guard also covers spreading into empty space, which is the usual way a water column would
/// otherwise pour over the hell boundary.
@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin implements SelfGetter<FlowingFluid> {
    @WrapOperation(method = "canSpreadTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;canBeReplacedWith(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/Direction;)Z"))
    private boolean cdp$preventWaterSpreadIntoUnderworld(FluidState instance, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction, Operation<Boolean> original) {
        if (direction == Direction.DOWN
                && pos.getY() < OverworldUtils.getCaveY()
                && level instanceof Level actualLevel
                && actualLevel.dimension() == OtherWorld.LEVEL
                && fluid.getFluidType() == NeoForgeMod.WATER_TYPE.value()
        ) return false;
        return original.call(instance, level, pos, fluid, direction);
    }

    @ModifyExpressionValue(method = "spreadToSides", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;getDropOff(Lnet/minecraft/world/level/LevelReader;)I"))
    private int longLava(int original, Level level, BlockPos pos, FluidState fluidState) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && level.dimension() == OtherWorld.LEVEL
                && fluidState.getFluidType() == NeoForgeMod.LAVA_TYPE.value()
        ) {
            return 1;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getNewLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;getDropOff(Lnet/minecraft/world/level/LevelReader;)I"))
    private int longLava(int original, Level level, BlockPos pos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && level.dimension() == OtherWorld.LEVEL
                && confluence$self().getFluidType() == NeoForgeMod.LAVA_TYPE.value()
        ) {
            return 1;
        }
        return original;
    }
}
