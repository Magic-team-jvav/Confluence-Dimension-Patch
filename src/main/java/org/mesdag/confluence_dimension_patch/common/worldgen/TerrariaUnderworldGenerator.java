package org.mesdag.confluence_dimension_patch.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.confluence.mod.common.init.ModBiomes;
import org.confluence.mod.common.init.block.NatureBlocks;
import org.confluence.mod.common.init.block.OreBlocks;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/// Terraria-style underworld built from stacked [NormalNoise] fields rather than vanilla
/// noise settings and density functions.
///
/// Terrain layout:
///
///   - a bedrock floor at the bottom;
///   - ash islands rooted in the floor, rising out of a lava base;
///   - rough, porous ash surfaces carved by 3D noise;
///   - a broken ash ceiling with stalactites;
///   - small hellstone pockets and scattered lava pools.
///
public class TerrariaUnderworldGenerator extends ChunkGenerator {
    public static final MapCodec<TerrariaUnderworldGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(TerrariaUnderworldGenerator::getBiomeSource)
    ).apply(instance, TerrariaUnderworldGenerator::new));

    private static final int LAVA_LEVEL = 38;
    private static final int BEDROCK_MIN = 1;
    private static final int BEDROCK_MAX = 9;

    private static final BlockState ASH = NatureBlocks.ASH_BLOCK.get().defaultBlockState();
    private static final BlockState ASH_GRASS = NatureBlocks.ASH_GRASS_BLOCK.get().defaultBlockState();
    private static final BlockState HELLSTONE = OreBlocks.ASH_HELLSTONE.get().defaultBlockState();
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();

    private long seed;
    private NormalNoise shapeNoise;
    private NormalNoise roofNoise;
    private NormalNoise hellstoneNoise;
    private NormalNoise bedrockNoise;
    private NormalNoise cheeseNoise;

    public TerrariaUnderworldGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    public void cdp$bind(long seed) {
        this.seed = seed;
        this.shapeNoise = NormalNoise.create(RandomSource.create(seed ^ 0x13198A2E03707344L), -4, 1.0, 1.0, 0.5);
        this.roofNoise = NormalNoise.create(RandomSource.create(seed ^ 0xA4093822299F31D0L), -3, 1.0, 1.0);
        this.hellstoneNoise = NormalNoise.create(RandomSource.create(seed ^ 0x452821E638D01377L), -2, 1.0, 1.0);
        this.bedrockNoise = NormalNoise.create(RandomSource.create(seed ^ 0x9E3779B97F4A7C15L), -3, 1.0, 1.0);
        this.cheeseNoise = NormalNoise.create(RandomSource.create(seed ^ 0xDB4F0B9175AE2165L), 0, 1.0, 1.0);
    }

    private int islandTop(int x, int z) {
        double offset = (seed & 0xFFL) * 0.37;
        double broad = valueNoise2D(x * 0.045 + offset, z * 0.045 - offset);
        double medium = valueNoise2D(x * 0.09 - offset, z * 0.09 + offset);
        double root = broad * 0.8 + medium * 0.2;
        int raw = 30 + (int) Math.round(root * 58.0);

        /// A slowly varying "mesa level" clamps the peak of the smooth height field. The result is
        /// a trapezoid: gently sloping flanks and a flat top, rather than quantized steps.
        double levelNoise = valueNoise2D(x * 0.012 - offset, z * 0.012 + offset);
        int level = 46 + (int) Math.round(levelNoise * 12.0);
        return OtherWorld.getUnderworldY() + Math.min(raw, level);
    }

    private int roofBottom(int x, int z) {
        double value = roofNoise.getValue(x * 0.04, 0.0, z * 0.04);
        return OverworldUtils.getCaveY() - 20 - (int) Math.round((value + 1.0) * 3.0);
    }

    private int stalactiteLength(int x, int z) {
        double value = roofNoise.getValue(x * 0.10 + 71.0, 0.0, z * 0.10 - 13.0);
        return Math.max(0, (int) Math.floor((value + 0.25) * 12.0));
    }

    /// The floor is deliberately uneven: a low-frequency noise gives broad rises, and a high
    /// frequency hash layer breaks the surface into jagged bedrock teeth.
    private int bedrockTop(int x, int z) {
        double broad = bedrockNoise.getValue(x * 0.07, 0.0, z * 0.07);
        double jagged = valueNoise2D(x * 0.25 + 17.0, z * 0.25 - 31.0);
        int height = BEDROCK_MIN + (int) Math.round((broad + 1.0) * 2.5 + jagged * 1.5);
        return OtherWorld.getUnderworldY() + Math.max(BEDROCK_MIN, Math.min(BEDROCK_MAX, height));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("cdp_fill_underworld", () -> {
            generate(chunk);
            return chunk;
        }), Util.backgroundExecutor());
    }

    private void generate(ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int bottom = OtherWorld.getUnderworldY();
        int top = OverworldUtils.getCaveY();
        int lavaLevel = bottom + LAVA_LEVEL;
        int minSection = chunk.getSectionIndex(bottom);
        int maxSection = chunk.getSectionIndex(top - 1);

        for (int sectionIndex = minSection; sectionIndex <= maxSection; sectionIndex++) {
            chunk.getSection(sectionIndex).acquire();
        }

        try {
            for (int localX = 0; localX < 16; localX++) {
                int x = minX + localX;
                for (int localZ = 0; localZ < 16; localZ++) {
                    int z = minZ + localZ;
                    int islandTop = islandTop(x, z);
                    double topDetail = shapeNoise.getValue(x * 0.33, 0.0, z * 0.33);
                    if (topDetail > 0.62) {
                        islandTop -= 1;
                    } else if (topDetail < -0.62) {
                        islandTop += 1;
                    }
                    int bedrockTop = bedrockTop(x, z);
                    int roofBottom = roofBottom(x, z);
                    int stalactite = stalactiteLength(x, z);
                    for (int y = top - 1; y >= bottom; y--) {
                        BlockState state = null;
                        if (y <= bedrockTop) {
                            state = BEDROCK;
                        } else if (y >= roofBottom || y > roofBottom - stalactite) {
                            state = ASH;
                        } else {
                            boolean solid = y <= islandTop;
                            if (solid) {
                                double vein = hellstoneNoise.getValue(x * 0.25 + 17.0, y * 0.25, z * 0.25 - 31.0);
                                state = y < lavaLevel + 20 && vein > 0.35 ? HELLSTONE : ASH;
                            } else if (y <= lavaLevel) {
                                state = LAVA;
                            }
                        }
                        if (state != null) {
                            setBlock(chunk, x, y, z, state);
                        }
                    }
                }
            }
        } finally {
            for (int sectionIndex = minSection; sectionIndex <= maxSection; sectionIndex++) {
                chunk.getSection(sectionIndex).release();
            }
        }
    }

    private static void setBlock(ChunkAccess chunk, int x, int y, int z, BlockState state) {
        chunk.getSection(chunk.getSectionIndex(y)).setBlockState(x & 15, y & 15, z & 15, state, false);
    }

    private static BlockState getBlock(ChunkAccess chunk, int x, int y, int z) {
        return chunk.getSection(chunk.getSectionIndex(y)).getBlockState(x & 15, y & 15, z & 15);
    }

    /**
     * Drills the tube network after the surface and the underworld terrain are both in place. The
     * tubes start at y=-57 (seven blocks above the hell boundary) and grow wider towards that top.
     */
    public void cdp$carveTunnels(WorldGenRegion level, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int bottom = OtherWorld.getUnderworldY();
        int lavaLevel = bottom + LAVA_LEVEL;
        int tunnelTop = OverworldUtils.getCaveY() + 24;
        RandomSource random = RandomSource.create(chunk.getPos().toLong() ^ seed ^ 0x9E3779B97F4A7C15L);

        for (int tunnel = 0; tunnel < 2; tunnel++) {
            double x = minX + random.nextDouble() * 16.0;
            double z = minZ + random.nextDouble() * 16.0;
            double y = tunnelTop;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle) * 0.35;
            double dz = Math.sin(angle) * 0.35;
            int length = 55 + random.nextInt(35);
            for (int step = 0; step < length && y > bottom + 8; step++) {
                double progress = Mth.clamp(
                        (y - (bottom + 8)) / (double) (tunnelTop - (bottom + 8)),
                        0.0,
                        1.0
                );
                /// Narrow at the bottom, about five blocks in diameter near y=-58.
                double radius = 1.5 + progress * 1.3;
                carveSphere(chunk, x, y, z, radius, minX, minZ, tunnelTop, bottom, lavaLevel);
                x += dx;
                z += dz;
                y -= 1.0;
                dx += (random.nextDouble() - 0.5) * 0.12;
                dz += (random.nextDouble() - 0.5) * 0.12;
            }
        }

        /// A few roughly horizontal tunnels through the lower ash, above the lava base.
        for (int tunnel = 0; tunnel < 3; tunnel++) {
            double x = minX + random.nextDouble() * 16.0;
            double z = minZ + random.nextDouble() * 16.0;
            int startX = (int) Math.floor(x);
            int startZ = (int) Math.floor(z);
            double y = Double.NaN;
            for (int scanY = lavaLevel + 30; scanY > bottom + 10; scanY--) {
                BlockState state = getBlock(chunk, startX, scanY, startZ);
                if (!state.isAir() && !state.is(LAVA.getBlock()) && !state.is(BEDROCK.getBlock())) {
                    y = scanY;
                    break;
                }
            }
            if (Double.isNaN(y)) {
                continue;
            }
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle) * 0.9;
            double dz = Math.sin(angle) * 0.9;
            double dy = (random.nextDouble() - 0.5) * 0.06;
            int length = 50 + random.nextInt(30);
            for (int step = 0; step < length && y > bottom + 8; step++) {
                carveSphere(chunk, x, y, z, 2.5, minX, minZ, tunnelTop, bottom, lavaLevel);
                x += dx;
                z += dz;
                y += dy;
            }
        }

        /// Swiss-cheese holes through the ash ceiling, open to the surface layer above.
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
                int ceilingBottom = roofBottom(x, z) - stalactiteLength(x, z);
                for (int y = tunnelTop; y >= ceilingBottom; y--) {
                    BlockState state = getBlock(chunk, x, y, z);
                    if (state.isAir() || state.is(LAVA.getBlock()) || state.is(BEDROCK.getBlock())) {
                        continue;
                    }
                    double hole = cheeseNoise.getValue(x * 0.09, y * 0.09, z * 0.09);
                    if (hole > 0.18) {
                        setBlock(chunk, x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        applyAshForestSurface(level, chunk);
    }

    /**
     * Confluence's nether surface rule is normally applied before carvers. Horizontal tunnels are
     * carved afterwards, so their newly exposed ash floors would otherwise lose the ash-grass
     * surface. Re-apply the grass rule to every exposed ash floor once the tunnels are finished.
     */
    private void applyAshForestSurface(WorldGenRegion level, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int bottom = OtherWorld.getUnderworldY();
        int lavaLevel = bottom + LAVA_LEVEL;
        int surfaceY = OverworldUtils.getCaveY() + 7;
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
                if (!chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(bottom + 20), QuartPos.fromBlock(z)).is(ModBiomes.ASH_FOREST)) {
                    continue;
                }
                for (int y = surfaceY; y > bottom + 1; y--) {
                    if (y - 1 <= lavaLevel) {
                        break;
                    }
                    if (!getBlock(chunk, x, y, z).isAir()) {
                        continue;
                    }
                    if (getBlock(chunk, x, y - 1, z).is(ASH.getBlock())) {
                        setBlock(chunk, x, y - 1, z, ASH_GRASS);
                    }
                }
            }
        }
    }

    private void carveSphere(ChunkAccess chunk, double centerX, double centerY, double centerZ, double radius,
                             int minX, int minZ, int tunnelTop, int bottom, int lavaLevel) {
        int r = (int) Math.ceil(radius);
        int bx0 = (int) Math.floor(centerX);
        int by0 = (int) Math.floor(centerY);
        int bz0 = (int) Math.floor(centerZ);
        for (int ox = -r; ox <= r; ox++) {
            for (int oy = -r; oy <= r; oy++) {
                for (int oz = -r; oz <= r; oz++) {
                    if (ox * ox + oy * oy + oz * oz > radius * radius) {
                        continue;
                    }
                    int x = bx0 + ox;
                    int y = by0 + oy;
                    int z = bz0 + oz;
                    if (x < minX || x >= minX + 16 || z < minZ || z >= minZ + 16) {
                        continue;
                    }
                    if (y > tunnelTop || y <= bottom + 8) {
                        continue;
                    }
                    BlockState state = getBlock(chunk, x, y, z);
                    if (state.isAir() || state.is(LAVA.getBlock()) || state.is(BEDROCK.getBlock())) {
                        continue;
                    }
                    setBlock(chunk, x, y, z, y <= lavaLevel ? LAVA : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /**
     * The original boundary between the vanilla deepslate layer and the underworld ash is a flat
     * y=-64 plane. This replaces it with a bedrock-like noisy transition: broad rises plus jagged
     * teeth, with a few interlocking pockets right at the contact.
     */
    private void shapeAshDeepslateInterface(ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int caveY = OverworldUtils.getCaveY();
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
                double broad = bedrockNoise.getValue(x * 0.07, 0.0, z * 0.07);
                double jagged = valueNoise2D(x * 0.28 + 17.0, z * 0.28 - 31.0);
                int boundary = caveY + (int) Math.round(broad * 2.5 + jagged * 2.0);
                for (int y = caveY - 10; y <= caveY + 10; y++) {
                    BlockState state = getBlock(chunk, x, y, z);
                    boolean isAsh = state.is(ASH.getBlock());
                    boolean isDeepslate = state.is(Blocks.DEEPSLATE);
                    if (!isAsh && !isDeepslate) {
                        continue;
                    }
                    boolean useAsh = y <= boundary;
                    if (Math.abs(y - boundary) <= 2) {
                        double pocket = shapeNoise.getValue(x * 0.35, y * 0.35, z * 0.35);
                        if (pocket > 0.35) {
                            useAsh = !useAsh;
                        }
                    }
                    setBlock(chunk, x, y, z, useAsh ? ASH : Blocks.DEEPSLATE.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        shapeAshDeepslateInterface(chunk);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int bottom = OtherWorld.getUnderworldY();
        int lavaLevel = bottom + LAVA_LEVEL;
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
                int startY = Math.min(islandTop(x, z) + 3, OverworldUtils.getCaveY() - 1);
                for (int y = startY; y > bottom; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    if (state.is(ASH.getBlock())) {
                        /// Confluence's nether surface rule: ash forest gets ash grass on the
                        /// exposed surface when it is above the lava sea, otherwise plain ash.
                        Holder<Biome> biome = chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
                        if (y > lavaLevel && biome.is(ModBiomes.ASH_FOREST)) {
                            setBlock(chunk, x, y, z, ASH_GRASS);
                        }
                    }
                    break;
                }
            }
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
        // Terrain porosity is part of the noise field.
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return OverworldUtils.getCaveY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        int minY = OtherWorld.getUnderworldY();
        BlockState[] states = new BlockState[OverworldUtils.getUnderworldGenerationHeight()];
        int islandTop = islandTop(x, z);
        int bedrockTop = bedrockTop(x, z);
        int lavaLevel = minY + LAVA_LEVEL;
        for (int i = 0; i < states.length; i++) {
            int y = minY + i;
            states[i] = y <= bedrockTop
                    ? BEDROCK
                    : y <= islandTop ? ASH : y <= lavaLevel ? LAVA : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public int getGenDepth() {
        return OverworldUtils.getUnderworldGenerationHeight();
    }

    @Override
    public int getSeaLevel() {
        return OtherWorld.getUnderworldY() + LAVA_LEVEL;
    }

    @Override
    public int getMinY() {
        return OtherWorld.getUnderworldY();
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
    }

    private static double valueNoise2D(double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        fx = fx * fx * (3.0 - 2.0 * fx);
        fz = fz * fz * (3.0 - 2.0 * fz);
        double v00 = hash2(x0, z0);
        double v10 = hash2(x0 + 1, z0);
        double v01 = hash2(x0, z0 + 1);
        double v11 = hash2(x0 + 1, z0 + 1);
        double a = v00 + (v10 - v00) * fx;
        double b = v01 + (v11 - v01) * fx;
        return (a + (b - a) * fz) * 2.0 - 1.0;
    }

    private static double hash2(int x, int z) {
        long hash = 0x9E3779B97F4A7C15L;
        hash ^= (long) x * 0xBF58476D1CE4E5B9L;
        hash ^= (long) z * 0x94D049BB133111EBL;
        hash ^= hash >>> 30;
        hash *= 0xBF58476D1CE4E5B9L;
        hash ^= hash >>> 27;
        return (hash >>> 11) * 0x1.0p-53;
    }
}
