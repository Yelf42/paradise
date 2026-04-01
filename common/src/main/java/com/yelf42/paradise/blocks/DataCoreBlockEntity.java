package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DataCoreBlockEntity extends BlockEntity {

    public DataCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DATA_CORE, pos, blockState);
    }
}
