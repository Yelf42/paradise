package com.yelf42.paradise;

import com.google.common.collect.ImmutableList;
import com.yelf42.paradise.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO custom structure placer code that can be configured for number of bunkers?
// TODO Verify all 48 bunkers within 16000 blocks

// TODO whitelist system
//  Set which players are whitelisted per paradise dimension (- nullspace)
//  Whitelist decays over time, needs to be maintained (configurable)
//  Can use a Reader or Downloader to paradise you aren't whitelisted in
//    - exception to this is if you hold a scrambler
//    - breaking in like this results in random teleport
//    - triggers intruder detection and firewall
//    - big penalty if intruder lets go of scrambler (long stun lock, teleport out after?)

// TODO whole dimension effects (maybe same place that whitelist is configured?)

// TODO infinite punch-through arrow/weapon?

// TODO giant digital asparagus

// TODO circle tool, creates weird stuff
// TODO scrambler device
public class Paradise {

    public static final String MOD_ID = "paradise";
    public static final String MOD_NAME = "Paradise";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"}; // Nice for components

    public static final PerlinSimplexNoise PERLIN = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(765341L)), ImmutableList.of(0));

    public static final TagKey<Item> DIGITAL_BLOCK_ITEMS = TagKey.create(Registries.ITEM, identifier( "digital_blocks"));
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