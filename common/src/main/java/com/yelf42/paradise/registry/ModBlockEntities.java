package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DataCoreBlockEntity;
import com.yelf42.paradise.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ModBlockEntities {
    public static final LinkedHashMap<String, BlockEntityType<?>> REGISTERED_BLOCK_ENTITIES = new LinkedHashMap<>();

    public static BlockEntityType<DataCoreBlockEntity> DATA_CORE = register("data_core", DataCoreBlockEntity::new, ModBlocks.DATA_CORE);


    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BiFunction<BlockPos, BlockState, T> function, Block block) {
        var blockEntity = Services.PLATFORM.blockEntityType(function, block);
        REGISTERED_BLOCK_ENTITIES.put(name, blockEntity);
        return blockEntity;
    }

    public static void register(BiConsumer<BlockEntityType<?>, ResourceLocation> consumer) {
        REGISTERED_BLOCK_ENTITIES.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
