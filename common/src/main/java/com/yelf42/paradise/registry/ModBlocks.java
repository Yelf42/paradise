package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModBlocks {
    public static final LinkedHashMap<String, Item> REGISTERED_BLOCK_ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Item> REGISTERED_CREATIVE_BLOCK_ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Block> REGISTERED_BLOCKS = new LinkedHashMap<>();

    public static final Block DIGITAL_GRASS_BLOCK = register(
            "digital_grass_block",
            DigitalGrassBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1F)
                    .sound(SoundType.GRASS)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DIGITAL_GRASS_BARRIER = registerCreative(
            "digital_grass_barrier",
            DigitalGrassBarrierBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.GRASS)
                    .noLootTable()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DIGITAL_BARRIER = registerCreative(
            "digital_barrier",
            DigitalBarrierBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DIGITAL_VOLUME = register(
            "digital_volume",
            DigitalVolumeBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(0.1F)
                    .randomTicks()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DIGITAL_PILLAR = register(
            "digital_pillar",
            DigitalPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(0.1F)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final Block DIGITAL_PILLAR_BARRIER = registerCreative(
            "digital_pillar_barrier",
            DigitalPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final Block DIGITAL_PILLAR_SLAB = register(
            "digital_pillar_slab",
            DigitalSlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(0.1F)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DIGITAL_VOLUME_BARRIER = registerCreative(
            "digital_volume_barrier",
            DigitalVolumeBarrierBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .randomTicks()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DIGITAL_UPLOADER = register(
            "digital_uploader",
            DigitalUploaderBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(0.1F)
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DATA_DOWNLOADER = register(
            "data_downloader",
            DataDownloaderBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(-1.0F, 3600000.8F)
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block DATA_CORE = registerCreative(
            "data_core",
            DataCoreBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .lightLevel((state) -> 15)
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final Block DATA_SERVER = registerCreative(
            "data_server",
            DataServerBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.RARE)
    );

    // TODO graphical issue on corner
    public static final Block DATA_SHIELD = registerCreative(
            "data_shield",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block DATA_READER = register(
            "data_reader",
            DataReaderBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5F, 3600000.8F)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .lightLevel((state) -> (state.getValue(DataReaderBlock.HAS_DISC)) * 6)
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block EMERGENCY_EXIT = registerCreative("emergency_exit",
            EmergencyExitBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noOcclusion()
                    .noLootTable()
                    .strength(-1.0F, 3600000.8F)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block DIGITAL_BULB = register(
            "digital_bulb",
            DigitalBulb::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.0F)
                    .noTerrainParticles()
                    .noOcclusion()
                    .noCollission()
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel((state) -> 15)
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        Block block = factory.apply(settings);
        REGISTERED_BLOCKS.put(name, block);
        REGISTERED_BLOCK_ITEMS.put(name, new BlockItem(block, itemSettings));
        return block;
    }

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return register(name, factory, settings, new Item.Properties());
    }

    public static Block registerCreative(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerCreative(name, factory, settings, new Item.Properties());
    }
    public static Block registerCreative(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        Block block = factory.apply(settings);
        REGISTERED_BLOCKS.put(name, block);
        REGISTERED_CREATIVE_BLOCK_ITEMS.put(name, new BlockItem(block, itemSettings));
        return block;
    }

    private static Block registerPotted(String name, BlockBehaviour.Properties settings, Block flower) {
        Block block = new FlowerPotBlock(flower, settings);
        REGISTERED_BLOCKS.put(name, block);
        return block;
    }

    /// BINDERS
    public static void registerItems(BiConsumer<Item, ResourceLocation> consumer) {
        REGISTERED_BLOCK_ITEMS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
        REGISTERED_CREATIVE_BLOCK_ITEMS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
    public static void registerBlocks(BiConsumer<Block, ResourceLocation> consumer) {
        REGISTERED_BLOCKS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
