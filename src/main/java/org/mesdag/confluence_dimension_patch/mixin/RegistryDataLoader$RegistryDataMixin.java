package org.mesdag.confluence_dimension_patch.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.confluence.mod.Confluence;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OnlyInOtherworldPlacementFilter;
import org.mesdag.confluence_dimension_patch.common.OnlyInOtherworldStructurePlacement;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.mesdag.confluence_dimension_patch.mixin.underworld.LevelStemAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RegistryDataLoader.RegistryData.class)
public abstract class RegistryDataLoader$RegistryDataMixin<T> {
    @Shadow
    @Final
    private ResourceKey<? extends Registry<T>> key;

    @Inject(method = "create", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private void modifyRegistry(CallbackInfoReturnable<Object> cir, @Local RegistryBuilder<T> builder) {
        if (Registries.PLACED_FEATURE.equals(key)) {
            builder.onAdd((registry, id, key, value) -> {
                if (Confluence.MODID.equals(key.location().getNamespace())) {
                    List<PlacementModifier> placement = ((PlacedFeature) value).placement();
                    ImmutableList.Builder<PlacementModifier> modifier = ImmutableList.builder();
                    modifier.add(OnlyInOtherworldPlacementFilter.INSTANCE);
                    if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration() && "ash_hellstone".equals(key.location().getPath())) {
                        for (PlacementModifier element : placement) {
                            modifier.add(element instanceof HeightRangePlacement ? HeightRangePlacement.triangle(
                                    VerticalAnchor.absolute(OtherWorld.getUnderworldY()),
                                    VerticalAnchor.absolute(OverworldUtils.getCaveY() - 1)
                            ) : element);
                        }
                    } else {
                        modifier.addAll(placement);
                    }
                    ((PlacedFeatureAccessor) value).setPlacement(modifier.build());
                }
            });
        } else if (Registries.STRUCTURE_SET.equals(key)) {
            builder.onAdd((registry, id, key, value) -> {
                if (Confluence.MODID.equals(key.location().getNamespace())) {
                    StructurePlacement placement = ((StructureSet) value).placement();
                    ((StructureSetAccessor) value).setPlacement(new OnlyInOtherworldStructurePlacement(placement));
                }
            });
        } else if (Registries.LEVEL_STEM.equals(key) && CDPStartupConfigs.enableTerrariaLikeWorldGeneration()) {
            builder.onAdd((registry, id, key, value) -> {
                if (OtherWorld.LEVEL_STEM.equals(key)) {
                    if (((LevelStem) value).type() instanceof Holder.Reference<DimensionType> reference
                            && reference.owner instanceof HolderLookup.RegistryLookup<DimensionType> lookup
                    ) ((LevelStemAccessor) value).setType(lookup.getOrThrow(OtherWorld.TERRARIA_DIMENSION_TYPE));
                }
            });
        }
    }
}
