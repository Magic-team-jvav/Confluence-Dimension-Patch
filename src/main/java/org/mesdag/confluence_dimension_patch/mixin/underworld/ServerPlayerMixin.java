package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @ModifyExpressionValue(method = "findRespawnAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/RespawnAnchorBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private static boolean checkUnderworldAnchor(boolean original, ServerLevel level, BlockPos pos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && level.dimension() == OtherWorld.LEVEL
        ) return true;
        return original;
    }

    @ModifyExpressionValue(method = "findRespawnAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private static boolean checkUnderworldBed(boolean original, ServerLevel level, BlockPos pos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && pos.getY() < OverworldUtils.getCaveY()
                && level.dimension() == OtherWorld.LEVEL
        ) return false;
        return original;
    }
}
