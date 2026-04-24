package com.yelf42.paradise.dimensions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DigitalGrassBarrierBlock;
import com.yelf42.paradise.blocks.DigitalGrassBlock;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ErrorChunkGenerator extends ChunkGenerator {

    private final String level;

    public ErrorChunkGenerator(RegistryAccess registryAccess, ResourceKey<Level> level) {
        this(new FixedBiomeSource(
                registryAccess.registryOrThrow(Registries.BIOME)
                        .getHolderOrThrow(ResourceKey.create(Registries.BIOME, Paradise.identifier("error")))
        ), level.location().getPath());
    }

    private final WorldgenRandom random;
    private final PerlinSimplexNoise noise;

    private ErrorChunkGenerator(BiomeSource biomeSource, String level) {
        super(biomeSource);
        this.random = new WorldgenRandom(new LegacyRandomSource(new Random().nextLong()));
        this.noise = new PerlinSimplexNoise(this.random, ImmutableList.of(0, 0, 0)); // 3 octaves
        this.level = level;
    }

    public static final MapCodec<ErrorChunkGenerator> CODEC = MapCodec.unit(
            () -> new ErrorChunkGenerator(null, "")
    );



    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // Return 0 — void world has no solid base height
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return 0;
    }

    // Return an empty column — no blocks anywhere by default
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        // optional, leave empty
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving step) {
        // No carvers in a pocket dimension
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        if (level.getRandom().nextBoolean()) return;

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.distanceSquared(new ChunkPos(0,0)) > 49) return;

        int x = chunkPos.getMinBlockX() + 1 + level.getRandom().nextInt(14);
        int z = chunkPos.getMinBlockZ() + 1 + level.getRandom().nextInt(14);
        int y = 7 + level.getRandom().nextInt(24);

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = z - 1; j <= z + 1; j++) {
                for (int k = 1; k <= y; k++) {
                    BlockPos pos = new BlockPos(i, k, j);
                    chunk.setBlockState(pos, ModBlocks.DIGITAL_PILLAR.defaultBlockState(), false);
                }
            }
        }
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 0;
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunk) {

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x > 12 || chunkPos.x < -12 || chunkPos.z > 12 || chunkPos.z < -12) return CompletableFuture.completedFuture(chunk);

        int hillAmplitude = 6; // how tall the hills are
        double scale = 0.08;  // lower = broader/smoother hills

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                boolean landNoise = (0.0 < this.noise.getValue(worldX * scale, worldZ * scale, false));

                double noiseVal = landNoise ? 0.4 + (this.noise.getValue(worldX * scale * 0.5, worldZ * scale * 0.5, false) + 1.0) / 2.0 : 0.0;

                double dist = (new Vector2i(worldX, worldZ)).distance(0, 0);
                double sCurve = 1.0 / (1.0 + Math.pow(Math.E, 0.2 * (dist - 90.0)));

                int surfaceHeight = (int) (noiseVal * hillAmplitude * sCurve);

                for (int y = chunk.getMinBuildHeight(); y <= surfaceHeight; y++) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    if (y == 0) {
                        if (noiseVal > 0.0 && dist < 100) {
                            chunk.setBlockState(pos, ModBlocks.DIGITAL_VOLUME_BARRIER.defaultBlockState(), false);
                        } else {
                            chunk.setBlockState(pos, ModBlocks.DIGITAL_BARRIER.defaultBlockState(), false);
                        }
                        continue;
                    }

                    chunk.setBlockState(pos, ModBlocks.DIGITAL_VOLUME.defaultBlockState(), false);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}