package org.mesdag.confluence_dimension_patch.mixin.underworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IBlockExtension.class)
public interface IBlockExtensionMixin {
    @Inject(method = "isFireSource", at = @At("HEAD"), cancellable = true)
    private void checkUnderworld(BlockState state, LevelReader level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY() &&
                level instanceof Level _level &&
                _level.dimension() == OtherWorld.LEVEL
        ) cir.setReturnValue(state.is(BlockTags.INFINIBURN_NETHER));
    }
}
