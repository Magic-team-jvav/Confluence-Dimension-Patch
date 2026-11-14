package org.mesdag.confluence_dimension_patch.mixin;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.levelgen.placement.PlacedFeature.class)
public interface PlacedFeatureAccessor {
    @Mutable
    @Accessor
    void setPlacement(List<PlacementModifier> placement);
}
