package org.mesdag.confluence_dimension_patch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.mesdag.confluence_dimension_patch.mixed.IDimensionAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow
    @Final
    private RandomState randomState;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void apply(CallbackInfo ci, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) ChunkGenerator generator) {
        if (level.dimension() == OtherWorld.LEVEL) {
            IDimensionAccessor.of(generator.getBiomeSource()).cdp$setIsOtherworld();
            IDimensionAccessor.of(randomState).cdp$setIsOtherworld();
        }
    }
}
