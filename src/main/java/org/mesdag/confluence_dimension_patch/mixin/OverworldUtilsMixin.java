package org.mesdag.confluence_dimension_patch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OverworldUtils.class, remap = false)
public abstract class OverworldUtilsMixin {
    @ModifyReturnValue(method = "dimension", at = @At("RETURN"))
    private static ResourceKey<Level> replaceDimension(ResourceKey<Level> original) {
        return OtherWorld.LEVEL;
    }

    @ModifyReturnValue(method = "dimensionType", at = @At("RETURN"))
    private static ResourceKey<DimensionType> replaceDimensionType(ResourceKey<DimensionType> original) {
        return OtherWorld.DIMENSION_TYPE;
    }
}
