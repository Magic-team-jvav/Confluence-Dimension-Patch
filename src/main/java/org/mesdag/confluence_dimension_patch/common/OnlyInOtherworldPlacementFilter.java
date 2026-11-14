package org.mesdag.confluence_dimension_patch.common;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;

public class OnlyInOtherworldPlacementFilter extends PlacementFilter {
    public static final OnlyInOtherworldPlacementFilter INSTANCE = new OnlyInOtherworldPlacementFilter();
    public static final Codec<OnlyInOtherworldPlacementFilter> CODEC = Codec.unit(INSTANCE);

    private OnlyInOtherworldPlacementFilter() {}

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return context.getLevel().getLevel().dimension() == OtherWorld.LEVEL;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ConfluenceDimensionPatch.ONLY_IN_OTHERWORLD_PLACEMENT_FILTER.get();
    }
}
