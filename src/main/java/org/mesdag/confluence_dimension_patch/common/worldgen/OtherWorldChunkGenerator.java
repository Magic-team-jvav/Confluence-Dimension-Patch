package org.mesdag.confluence_dimension_patch.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.confluence.mod.common.worldgen.BannedBiomeNoiseBasedChunkGenerator;
import org.confluence.mod.mixed.IInjectChunkGenerator;
import org.confluence.mod.util.OverworldUtils;
import org.jetbrains.annotations.Nullable;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.mesdag.confluence_dimension_patch.mixed.IDimensionAccessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Generates the otherworld dimension as two stacked generators:
///
///   - the surface generator fills `-64..320`;
///   - the underworld generator fills `-192..-64` with Terraria's underworld layout.
///
/// The underworld part is only generated while
/// [#enableTerrariaLikeWorldGeneration()] is enabled.
public class OtherWorldChunkGenerator extends NoiseBasedChunkGenerator implements IInjectChunkGenerator {
    public static final Codec<OtherWorldChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NoiseBasedChunkGenerator.CODEC.fieldOf("surface").forGetter(OtherWorldChunkGenerator::surface),
            TerrariaUnderworldGenerator.CODEC.fieldOf("underworld").forGetter(OtherWorldChunkGenerator::underworld)
    ).apply(instance, OtherWorldChunkGenerator::new));

    private final NoiseBasedChunkGenerator surface;
    private final TerrariaUnderworldGenerator underworld;
    private final StackedBiomeSource stackedBiomeSource;
    private SurfaceRules.RuleSource overrideSurfaceRule;

    public OtherWorldChunkGenerator(NoiseBasedChunkGenerator surface, TerrariaUnderworldGenerator underworld) {
        this(surface, underworld, new StackedBiomeSource(surface.getBiomeSource(), underworld.getBiomeSource()));
    }

    private OtherWorldChunkGenerator(NoiseBasedChunkGenerator surface, TerrariaUnderworldGenerator underworld, StackedBiomeSource stackedBiomeSource) {
        super(stackedBiomeSource, surface.generatorSettings());
        this.surface = surface;
        this.underworld = underworld;
        this.stackedBiomeSource = stackedBiomeSource;
    }

    public NoiseBasedChunkGenerator surface() {
        return surface;
    }

    public TerrariaUnderworldGenerator underworld() {
        return underworld;
    }

    @Override
    public BiomeSource getBiomeSource() {
        return stackedBiomeSource;
    }

    /// Called from the `ChunkMap` constructor, where the level seed is available.
    public void cdp$bind(ServerLevel level) {
        underworld.cdp$bind(level.getSeed());
        IDimensionAccessor.of(surface.getBiomeSource()).cdp$setIsOtherworld();
        IDimensionAccessor.of(underworld.getBiomeSource()).cdp$setIsOtherworld();
    }

    private boolean hasUnderworld() {
        return CDPStartupConfigs.enableTerrariaLikeWorldGeneration();
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            chunk.fillBiomesFromNoise(this.biomeSource, randomState.sampler());
            return chunk;
        }), Util.backgroundExecutor());
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        if (!hasUnderworld()) {
            return surface.fillFromNoise(executor, blender, randomState, structureManager, chunk);
        }
        return surface.fillFromNoise(executor, blender, randomState, structureManager, chunk)
                .thenCompose(filled -> {
                    filled.noiseChunk = null;
                    return underworld.fillFromNoise(executor, blender, randomState, structureManager, filled);
                })
                .thenApply(filled -> {
                    filled.noiseChunk = null;
                    return filled;
                });
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        chunk.noiseChunk = null;
        surface.buildSurface(level, structureManager, random, chunk);
        if (hasUnderworld()) {
            chunk.noiseChunk = null;
            underworld.buildSurface(level, structureManager, random, chunk);
            chunk.noiseChunk = null;
        }
    }

    @Override
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step
    ) {
        chunk.noiseChunk = null;
        surface.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
        chunk.noiseChunk = null;
        if (hasUnderworld() && step == GenerationStep.Carving.AIR) {
            underworld.cdp$carveTunnels(level, chunk);
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return surface.getBaseHeight(x, z, type, level, random);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        NoiseColumn upper = surface.getBaseColumn(x, z, height, random);
        NoiseColumn lower = hasUnderworld() ? underworld.getBaseColumn(x, z, height, random) : null;
        int minY = height.getMinBuildHeight();
        BlockState[] states = new BlockState[height.getHeight()];
        for (int i = 0; i < states.length; i++) {
            int y = minY + i;
            states[i] = y < OverworldUtils.getCaveY() && lower != null ? lower.getBlock(y) : upper.getBlock(y);
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        surface.spawnOriginalMobs(level);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return surface.getSpawnHeight(level);
    }

    @Override
    public int getGenDepth() {
        return hasUnderworld() ? OtherWorld.getTerrariaHeight() : surface.getGenDepth();
    }

    @Override
    public int getSeaLevel() {
        return surface.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return hasUnderworld() ? OtherWorld.getTerrariaMinY() : surface.getMinY();
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        surface.addDebugScreenInfo(info, random, pos);
    }

    @Override
    public SurfaceRules.RuleSource confluence$getOriginalSurfaceRules() {
        return overrideSurfaceRule;
    }

    @Override
    public void confluence$setOverrideSurfaceRules(SurfaceRules.RuleSource ruleSource) {
        this.overrideSurfaceRule = ruleSource;
    }

    @Override
    public BiomeSource confluence$getGenerationBiomeSource() {
        return surface.getBiomeSource();
    }

    @Override
    public @Nullable BannedBiomeNoiseBasedChunkGenerator confluence$getBannedGenerator() {
        return surface instanceof BannedBiomeNoiseBasedChunkGenerator generator ? generator : null;
    }
}

