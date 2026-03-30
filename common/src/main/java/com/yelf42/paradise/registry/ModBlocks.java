package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DigitalBarrierBlock;
import com.yelf42.paradise.blocks.DigitalGrassBarrierBlock;
import com.yelf42.paradise.blocks.DigitalGrassBlock;
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
    public static final LinkedHashMap<String, Block> REGISTERED_BLOCKS = new LinkedHashMap<>();

    public static final Block DIGITAL_GRASS = register(
            "digital_grass",
            DigitalGrassBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false)),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block DIGITAL_GRASS_BARRIER = register(
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
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block DIGITAL_BARRIER = register(
            "digital_barrier",
            DigitalBarrierBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.AMETHYST)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                    .noTerrainParticles()
                    .pushReaction(PushReaction.BLOCK),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        Block block = factory.apply(settings);
        REGISTERED_BLOCKS.put(name, block);
        REGISTERED_BLOCK_ITEMS.put(name, new BlockItem(block, itemSettings));
        return block;
    }

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        Block block = factory.apply(settings);
        REGISTERED_BLOCKS.put(name, block);
        if (shouldRegisterItem) {
            REGISTERED_BLOCK_ITEMS.put(name, new BlockItem(block, new Item.Properties()));
        }
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
    }
    public static void registerBlocks(BiConsumer<Block, ResourceLocation> consumer) {
        REGISTERED_BLOCKS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
