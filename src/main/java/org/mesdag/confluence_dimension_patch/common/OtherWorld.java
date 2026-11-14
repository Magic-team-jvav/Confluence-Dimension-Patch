package org.mesdag.confluence_dimension_patch.common;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.confluence.mod.common.init.ModBiomes;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.confluence_dimension_patch.common.worldgen.OtherWorldChunkGenerator;
import org.mesdag.confluence_dimension_patch.common.worldgen.TerrariaUnderworldGenerator;
import org.mesdag.confluence_dimension_patch.mixin.underworld.PlayerMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

public final class OtherWorld {
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ConfluenceDimensionPatch.asResource("otherworld"));
    public static final ResourceKey<DimensionType> TERRARIA_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ConfluenceDimensionPatch.asResource("terraria"));
    public static final ResourceKey<LevelStem> LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, ConfluenceDimensionPatch.asResource("otherworld"));
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, ConfluenceDimensionPatch.asResource("otherworld"));
    public static final ResourceKey<NoiseGeneratorSettings> SURFACE_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, ConfluenceDimensionPatch.asResource("surface"));
    public static final ResourceLocation EFFECTS = ConfluenceDimensionPatch.asResource("effects");

    /// The surface part keeps the vanilla overworld height and terrain. The only change is that the
    /// bedrock floor is removed, so the overworld layer opens directly into the underworld layer.
    private static NoiseGeneratorSettings surfaceSettings(BootstapContext<NoiseGeneratorSettings> context) {
        NoiseGeneratorSettings overworld = NoiseGeneratorSettings.overworld(context, false, false);
        return new NoiseGeneratorSettings(
                overworld.noiseSettings(),
                overworld.defaultBlock(),
                overworld.defaultFluid(),
                overworld.noiseRouter(),
                net.minecraft.world.level.levelgen.SurfaceRules.sequence(
                        org.confluence.mod.common.worldgen.biome.SurfaceRuleData.makeConfluenceOverWorldRules(),
                        org.confluence.mod.common.worldgen.biome.SurfaceRuleData.makeMinecraftOverWorldRules(),
                        net.minecraft.data.worldgen.SurfaceRuleData.overworldLike(true, false, false)
                ),
                overworld.spawnTarget(),
                overworld.seaLevel(),
                overworld.disableMobGeneration(),
                overworld.isAquifersEnabled(),
                overworld.oreVeinsEnabled(),
                overworld.useLegacyRandomSource()
        );
    }

    public static void dimensionType(BootstapContext<DimensionType> context) {
        context.register(DIMENSION_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0,
                true,
                false,
                -64,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
        ));
        /// hasCeiling
        /// ultraWarm [org.mesdag.confluence_dimension_patch.mixin.underworld.LavaFluidMixin] [org.mesdag.confluence_dimension_patch.mixin.underworld.LiquidBlockMixin] [org.mesdag.confluence_dimension_patch.mixin.underworld.FlowingFluidMixin]
        /// natural
        /// bedWorks [org.mesdag.confluence_dimension_patch.mixin.underworld.BedBlockMixin] [PlayerMixin#checkUnderworldBed]
        /// respawnAnchorWorks [org.mesdag.confluence_dimension_patch.mixin.underworld.RespawnAnchorBlockMixin] [PlayerMixin#checkUnderworldAnchor]
        /// infiniburn [org.mesdag.confluence_dimension_patch.coremod.CDPCoreModHooks]
        context.register(TERRARIA_DIMENSION_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0,
                true,
                false,
                getTerrariaMinY(),
                getTerrariaHeight(),
                getTerrariaHeight(),
                BlockTags.INFINIBURN_OVERWORLD,
                EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
        ));
    }

    public static void noiseSettings(BootstapContext<NoiseGeneratorSettings> context) {
        context.register(SURFACE_NOISE_SETTINGS, surfaceSettings(context));
    }

    public static void levelStem(BootstapContext<LevelStem> context) {
        Holder.Reference<DimensionType> dimensionType = context.lookup(Registries.DIMENSION_TYPE).getOrThrow(DIMENSION_TYPE);
        Holder.Reference<NoiseGeneratorSettings> surfaceSettings = context.lookup(Registries.NOISE_SETTINGS).getOrThrow(SURFACE_NOISE_SETTINGS);
        Holder.Reference<MultiNoiseBiomeSourceParameterList> multiNoiseBiomeSourceParameterList = context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

        NoiseBasedChunkGenerator surface = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromPreset(multiNoiseBiomeSourceParameterList),
                surfaceSettings
        );
        TerrariaUnderworldGenerator underworld = new TerrariaUnderworldGenerator(createUnderworldBiomeSource(context));
        context.register(LEVEL_STEM, new LevelStem(dimensionType, new OtherWorldChunkGenerator(surface, underworld)));
    }

    /// Only the ash biomes are used in the underworld layer. Vanilla Nether features use absolute
    /// Y ranges authored for {@code 0..128}, which would not generate in the shifted layer; the ash
    /// biomes already contain Confluence's Terraria-style features and spawns.
    private static MultiNoiseBiomeSource createUnderworldBiomeSource(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> entries = new ArrayList<>();
        entries.add(Pair.of(
                Climate.parameters(
                        Climate.Parameter.span(-0.3F, 1.0F),
                        Climate.Parameter.span(-0.3F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        0.0F
                ),
                biomes.getOrThrow(ModBiomes.ASH_FOREST)
        ));
        entries.add(Pair.of(
                Climate.parameters(
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        Climate.Parameter.span(-1.0F, 1.0F),
                        0.1F
                ),
                biomes.getOrThrow(ModBiomes.ASH_WASTELAND)
        ));
        return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(entries));
    }

    public static int getTerrariaMinY() {
        return OverworldUtils.getCaveY() - OverworldUtils.getUnderworldGenerationHeight();
    }

    public static int getTerrariaHeight() {
        return 384 + OverworldUtils.getUnderworldGenerationHeight();
    }

    public static int getUnderworldY() {
        return getTerrariaMinY();
    }
}
