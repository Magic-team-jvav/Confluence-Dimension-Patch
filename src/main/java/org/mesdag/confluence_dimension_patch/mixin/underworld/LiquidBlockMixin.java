package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {
    @WrapOperation(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void fastLava1(Level instance, BlockPos pos, Fluid fluid, int delay, Operation<Void> original) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && instance.dimension() == OtherWorld.LEVEL
                && fluid.getFluidType() == ForgeMod.LAVA_TYPE.get()
        ) delay = 10;
        original.call(instance, pos, fluid, delay);
    }

    @WrapOperation(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void fastLava2(LevelAccessor instance, BlockPos pos, Fluid fluid, int delay, Operation<Void> original) {
        if (pos.getY() < OverworldUtils.getCaveY()
                && instance instanceof Level level
                && level.dimension() == OtherWorld.LEVEL
                && fluid.getFluidType() == ForgeMod.LAVA_TYPE.get()
        ) delay = 10;
        original.call(instance, pos, fluid, delay);
    }

    @WrapOperation(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void fastLava3(Level instance, BlockPos pos, Fluid fluid, int delay, Operation<Void> original) {
        if (pos.getY() < OverworldUtils.getCaveY()
                && instance.dimension() == OtherWorld.LEVEL
                && fluid.getFluidType() == ForgeMod.LAVA_TYPE.get()
        ) delay = 10;
        original.call(instance, pos, fluid, delay);
    }
}
