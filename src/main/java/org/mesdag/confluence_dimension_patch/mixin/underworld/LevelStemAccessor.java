package org.mesdag.confluence_dimension_patch.mixin.underworld;

import net.minecraft.core.Holder;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.dimension.LevelStem.class)
public interface LevelStemAccessor {
    @Mutable
    @Accessor
    void setType(Holder<DimensionType> type);
}
