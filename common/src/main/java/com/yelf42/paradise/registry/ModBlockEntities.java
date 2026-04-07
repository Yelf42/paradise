package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.*;
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
    public static BlockEntityType<DataSeverBlockEntity> DATA_SERVER = register("data_server", DataSeverBlockEntity::new, ModBlocks.DATA_SERVER);
    public static BlockEntityType<DataReaderBlockEntity> DATA_READER = register("data_reader", DataReaderBlockEntity::new, ModBlocks.DATA_READER);

    public static BlockEntityType<DigitalUploaderBlockEntity> DIGITAL_UPLOADER = register("digital_uploader", DigitalUploaderBlockEntity::new, ModBlocks.DIGITAL_UPLOADER);
    public static BlockEntityType<EmergencyExitBlockEntity> EMERGENCY_EXIT = register("emergency_exit", EmergencyExitBlockEntity::new, ModBlocks.EMERGENCY_EXIT);
    public static BlockEntityType<DataDownloaderBlockEntity> DATA_DOWNLOADER = register("data_downloader", DataDownloaderBlockEntity::new, ModBlocks.DATA_DOWNLOADER);


    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BiFunction<BlockPos, BlockState, T> function, Block block) {
        var blockEntity = Services.PLATFORM.blockEntityType(function, block);
        REGISTERED_BLOCK_ENTITIES.put(name, blockEntity);
        return blockEntity;
    }

    public static void register(BiConsumer<BlockEntityType<?>, ResourceLocation> consumer) {
        REGISTERED_BLOCK_ENTITIES.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
