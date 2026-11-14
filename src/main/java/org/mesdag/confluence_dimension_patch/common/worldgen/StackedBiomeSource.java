package org.mesdag.confluence_dimension_patch.common.worldgen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.confluence.mod.common.worldgen.BannedBiomeMultiNoiseBiomeSource;
import org.confluence.mod.common.worldgen.biome.injector.BiomeSourceHandler;
import org.confluence.mod.mixed.IInjectBiomeSource;
import org.confluence.mod.util.OverworldUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/// A biome source that uses the overworld biome source above `y = -64` and the ash hell
/// biome source below it.
public class StackedBiomeSource extends BiomeSource implements IInjectBiomeSource {
    public static final Codec<StackedBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("surface").forGetter(StackedBiomeSource::surface),
            BiomeSource.CODEC.fieldOf("underworld").forGetter(StackedBiomeSource::underworld)
    ).apply(instance, StackedBiomeSource::new));

    private final BiomeSource surface;
    private final BiomeSource underworld;
    private @Nullable BiomeSourceHandler handler;

    public StackedBiomeSource(BiomeSource surface, BiomeSource underworld) {
        this.surface = surface;
        this.underworld = underworld;
    }

    public BiomeSource surface() {
        return surface;
    }

    public BiomeSource underworld() {
        return underworld;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(surface.possibleBiomes().stream(), underworld.possibleBiomes().stream());
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (QuartPos.toBlock(y) < OverworldUtils.getCaveY()) {
            return underworld.getNoiseBiome(x, y, z, sampler);
        }
        return surface.getNoiseBiome(x, y, z, sampler);
    }

    @Override
    public void confluence$setHandler(@Nullable BiomeSourceHandler handler) {
        this.handler = handler;
    }

    @Override
    public @Nullable BiomeSourceHandler confluence$getHandler() {
        return handler;
    }

    @Override
    public @Nullable Pair<Holder<Biome>, Holder<Biome>> confluence$getBiomePair() {
        return surface instanceof IInjectBiomeSource source ? source.confluence$getBiomePair() : null;
    }

    @Override
    public List<Holder<Biome>> confluence$getJungleBiomes() {
        return surface instanceof IInjectBiomeSource source
                ? source.confluence$getJungleBiomes()
                : IInjectBiomeSource.super.confluence$getJungleBiomes();
    }

    @Override
    public @Nullable BannedBiomeMultiNoiseBiomeSource confluence$getBannedBiomeSource() {
        return surface instanceof IInjectBiomeSource source ? source.confluence$getBannedBiomeSource() : null;
    }
}
