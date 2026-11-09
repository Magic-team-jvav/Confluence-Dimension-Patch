package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {
    @ModifyExpressionValue(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private boolean checkUnderworld(boolean original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration() && OverworldUtils.inUnderworld(level.dimension(), pos.getY())) {
            return false;
        }
        return original;
    }
}
