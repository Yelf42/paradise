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

public class ParadiseChunkGenerator extends ChunkGenerator {

    private final String level;

    public ParadiseChunkGenerator(RegistryAccess registryAccess, ResourceKey<Level> level) {
        this(new FixedBiomeSource(
                registryAccess.registryOrThrow(Registries.BIOME)
                        .getHolderOrThrow(ResourceKey.create(Registries.BIOME, Paradise.identifier("digital_biome")))
        ), level.location().getPath());
    }

    private final WorldgenRandom random;
    private final PerlinSimplexNoise noise;

    private ParadiseChunkGenerator(BiomeSource biomeSource, String level) {
        super(biomeSource);
        this.random = new WorldgenRandom(new LegacyRandomSource(new Random().nextLong()));
        this.noise = new PerlinSimplexNoise(this.random, ImmutableList.of(0, 0, 0)); // 3 octaves
        this.level = level;
    }

    public static final MapCodec<ParadiseChunkGenerator> CODEC = MapCodec.unit(
            () -> new ParadiseChunkGenerator((BiomeSource) null, "")
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
        // Leave empty unless you want decoration/structures
        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 3) {
            if (chunkPos.z == 0) {
                for (int x = 7; x < 10; x++) {
                    for (int y = 0; y < 12; y++) {
                        for (int z = 0; z < 2; z++) {
                            int worldX = chunkPos.getMinBlockX() + x;
                            int worldZ = chunkPos.getMinBlockZ() + z;
                            BlockPos pos = new BlockPos(worldX, y, worldZ);
                            if (y < 6) {
                                chunk.setBlockState(pos, ModBlocks.DIGITAL_PILLAR_BARRIER.defaultBlockState(), false);
                            } else {
                                chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                            }
                        }
                    }
                }
                BlockPos exitSignPos = new BlockPos(chunkPos.getMinBlockX() + 8, 2, chunkPos.getMinBlockZ());
                chunk.setBlockState(exitSignPos, ModBlocks.EMERGENCY_EXIT.defaultBlockState(), false);
                BlockEntity blockEntity = ModBlockEntities.EMERGENCY_EXIT.create(exitSignPos, ModBlocks.EMERGENCY_EXIT.defaultBlockState());
                if (blockEntity != null) {
                    chunk.setBlockEntity(blockEntity);
                }
                chunk.setBlockState(exitSignPos.above(1), Blocks.AIR.defaultBlockState(), false);
                chunk.setBlockState(exitSignPos.above(2), Blocks.AIR.defaultBlockState(), false);

                chunk.setBlockState(exitSignPos.east(), ModBlocks.DIGITAL_PILLAR_SLAB.defaultBlockState(), false);
                chunk.setBlockState(exitSignPos.east().above(1), Blocks.AIR.defaultBlockState(), false);
                chunk.setBlockState(exitSignPos.east().above(2), Blocks.AIR.defaultBlockState(), false);

            } else if (chunkPos.z == -1) {
                for (int x = 7; x < 10; x++) {
                    for (int y = 0; y < 10; y++) {
                        int worldX = chunkPos.getMinBlockX() + x;
                        int worldZ = chunkPos.getMinBlockZ() + 15;
                        BlockPos pos = new BlockPos(worldX, y, worldZ);
                        if (y < 6) {
                            chunk.setBlockState(pos, ModBlocks.DIGITAL_PILLAR_BARRIER.defaultBlockState(), false);
                        } else {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }

                    }
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
        double scale = 0.015;  // lower = broader/smoother hills

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                double noiseVal = (this.noise.getValue(worldX * scale, worldZ * scale, false) + 1.0) / 2.0;

                double dist = (new Vector2i(worldX, worldZ)).distance(0, 0);
                double sCurve = 1.0 / (1.0 + Math.pow(Math.E, 0.2 * (dist - 75.0)));

                int surfaceHeight = (int) (sCurve + noiseVal * hillAmplitude * sCurve);

                for (int y = chunk.getMinBuildHeight(); y <= surfaceHeight; y++) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    if (y < surfaceHeight) {
                        if (y==0) {
                            chunk.setBlockState(pos, ModBlocks.DIGITAL_VOLUME_BARRIER.defaultBlockState(), false);
                        } else {
                            chunk.setBlockState(pos, ModBlocks.DIGITAL_VOLUME.defaultBlockState(), false);
                        }
                        continue;
                    }

                    if (y==0) {
                        if (dist <= 80) {
                            chunk.setBlockState(pos, grassBarrierStateForPos(pos, this.level), false);
                        } else if (dist > 80 && dist < 128) {
                            double distT = (dist - 80.0) / 48.0;
                            double noiseScale = 0.01 + distT * 0.08;
                            double edgeNoiseVal = (this.noise.getValue(worldX * noiseScale, worldZ * noiseScale, false) + 1.0) / 2.0;

                            if (edgeNoiseVal > distT) {
                                chunk.setBlockState(pos, grassBarrierStateForPos(pos, this.level), false);
                            } else {
                                chunk.setBlockState(pos, ModBlocks.DIGITAL_BARRIER.defaultBlockState(), false);
                            }
                        } else {
                            chunk.setBlockState(pos, ModBlocks.DIGITAL_BARRIER.defaultBlockState(), false);
                        }
                    } else {
                        chunk.setBlockState(pos, grassStateForPos(pos, this.level), false);
                    }
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

    private BlockState grassStateForPos(BlockPos pos, String level) {
        int offset = DigitalGrassBlock.getOffsetForPos(pos, level);
        return ModBlocks.DIGITAL_GRASS_BLOCK.defaultBlockState().setValue(DigitalGrassBlock.OFFSET, offset);
    }
    private BlockState grassBarrierStateForPos(BlockPos pos, String level) {
        int offset = DigitalGrassBlock.getOffsetForPos(pos, level);
        return ModBlocks.DIGITAL_GRASS_BARRIER.defaultBlockState().setValue(DigitalGrassBarrierBlock.OFFSET,  offset);
    }
}