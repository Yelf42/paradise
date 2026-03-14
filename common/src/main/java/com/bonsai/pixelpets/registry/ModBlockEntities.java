package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.platform.Services;
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

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BiFunction<BlockPos, BlockState, T> function, Block block) {
        var blockEntity = Services.PLATFORM.blockEntityType(function, block);
        REGISTERED_BLOCK_ENTITIES.put(name, blockEntity);
        return blockEntity;
    }

    public static void register(BiConsumer<BlockEntityType<?>, ResourceLocation> consumer) {
        REGISTERED_BLOCK_ENTITIES.forEach((key, value) -> consumer.accept(value, PixelPets.identifier(key)));
    }
}
