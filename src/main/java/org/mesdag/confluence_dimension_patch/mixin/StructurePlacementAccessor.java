package org.mesdag.confluence_dimension_patch.mixin;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.levelgen.structure.placement.StructurePlacement.class)
public interface StructurePlacementAccessor {
    @Invoker
    Vec3i callLocateOffset();

    @Invoker
    StructurePlacement.FrequencyReductionMethod callFrequencyReductionMethod();

    @Invoker
    float callFrequency();

    @Invoker
    int callSalt();

    @Invoker
    Optional<StructurePlacement.ExclusionZone> callExclusionZone();

    @Invoker
    boolean callIsPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z);
}
