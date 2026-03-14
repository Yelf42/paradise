package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModBlocks {
    public static final LinkedHashMap<String, Item> REGISTERED_BLOCK_ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Block> REGISTERED_BLOCKS = new LinkedHashMap<>();

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
        REGISTERED_BLOCK_ITEMS.forEach((key, value) -> consumer.accept(value, PixelPets.identifier(key)));
    }
    public static void registerBlocks(BiConsumer<Block, ResourceLocation> consumer) {
        REGISTERED_BLOCKS.forEach((key, value) -> consumer.accept(value, PixelPets.identifier(key)));
    }
}
