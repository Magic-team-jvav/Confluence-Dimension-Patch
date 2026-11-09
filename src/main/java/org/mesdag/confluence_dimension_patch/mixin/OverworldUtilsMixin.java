package org.mesdag.confluence_dimension_patch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OverworldUtils.class)
public abstract class OverworldUtilsMixin {
    @ModifyReturnValue(method = "dimension", at = @At("RETURN"))
    private static ResourceKey<Level> replaceDimension(ResourceKey<Level> original) {
        return OtherWorld.LEVEL;
    }

    @ModifyReturnValue(method = "dimensionType", at = @At("RETURN"))
    private static ResourceKey<DimensionType> replaceDimensionType(ResourceKey<DimensionType> original) {
        return CDPStartupConfigs.enableTerrariaLikeWorldGeneration() ? OtherWorld.TERRARIA_DIMENSION_TYPE : OtherWorld.DIMENSION_TYPE;
    }

    @ModifyReturnValue(method = "underworld", at = @At("RETURN"))
    private static ResourceKey<Level> replaceUnderworld(ResourceKey<Level> original) {
        return CDPStartupConfigs.enableTerrariaLikeWorldGeneration() ? OtherWorld.LEVEL : original;
    }

    @ModifyReturnValue(method = "underworldType", at = @At("RETURN"))
    private static ResourceKey<DimensionType> replaceUnderworldType(ResourceKey<DimensionType> original) {
        return CDPStartupConfigs.enableTerrariaLikeWorldGeneration() ? OtherWorld.TERRARIA_DIMENSION_TYPE : original;
    }

    @ModifyReturnValue(method = "inUnderworld", at = @At("RETURN"))
    private static boolean replace(boolean original, ResourceKey<Level> dimension, double y) {
        if (dimension == OtherWorld.LEVEL && CDPStartupConfigs.enableTerrariaLikeWorldGeneration()) {
            return y >= OtherWorld.getUnderworldY() && y < OverworldUtils.getCaveY();
        }
        return original;
    }
}
