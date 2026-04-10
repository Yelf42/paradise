package com.yelf42.paradise;

import com.google.common.collect.ImmutableList;
import com.yelf42.paradise.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO override fishing in digital biomes
public class Paradise {

    public static final String MOD_ID = "paradise";
    public static final String MOD_NAME = "Paradise";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"}; // Nice for components

    public static final Vector3f PARADISE_SKY = new Vector3f(60/ 255.0f, 119/ 255.0f, 239/ 255.0f);
    public static final Vector3f PARADISE_SKY_REFLECTION = new Vector3f(30/ 255.0f, 100/ 255.0f, 239/ 255.0f);

    public static final PerlinSimplexNoise PERLIN = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(765341L)), ImmutableList.of(0));

    public static final TagKey<Block> DIGITAL_BLOCKS = TagKey.create(Registries.BLOCK, identifier( "digital_blocks"));

    public static final TagKey<DimensionType> PARADISE_DIMENSIONS = TagKey.create(
            Registries.DIMENSION_TYPE,
            Paradise.identifier("paradise_dimensions")
    );

    public static void init() {
        LOGGER.info("Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(Paradise.MOD_ID, path);
    }
}