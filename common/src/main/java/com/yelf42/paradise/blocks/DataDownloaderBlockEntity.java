package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DataDownloaderBlockEntity extends DigitalSymbolBlockEntity {
    public DataDownloaderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DATA_DOWNLOADER, pos, blockState);
    }

    @Override
    public boolean shouldRender() {
        return this.getBlockState().getValue(DataDownloaderBlock.POWERED);
    }
}
