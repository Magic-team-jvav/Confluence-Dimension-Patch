package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyExpressionValue(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/RespawnAnchorBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private static boolean checkUnderworldAnchor(boolean original, ServerLevel serverLevel, BlockPos spawnBlockPos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && spawnBlockPos.getY() < OverworldUtils.getCaveY()
                && serverLevel.dimension() == OtherWorld.LEVEL
        ) return true;
        return original;
    }

    @ModifyExpressionValue(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private static boolean checkUnderworldBed(boolean original, ServerLevel serverLevel, BlockPos spawnBlockPos) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()
                && spawnBlockPos.getY() < OverworldUtils.getCaveY()
                && serverLevel.dimension() == OtherWorld.LEVEL
        ) return false;
        return original;
    }
}
