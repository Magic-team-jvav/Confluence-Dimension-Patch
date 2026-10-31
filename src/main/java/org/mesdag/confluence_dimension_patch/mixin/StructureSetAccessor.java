package org.mesdag.confluence_dimension_patch.mixin;

import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.levelgen.structure.StructureSet.class)
public interface StructureSetAccessor {
    @Mutable
    @Accessor
    void setPlacement(StructurePlacement placement);
}
