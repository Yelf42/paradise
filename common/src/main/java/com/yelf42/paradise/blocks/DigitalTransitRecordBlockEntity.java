package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DigitalTransitRecordBlockEntity extends AbstractDigitalSymbolBlockEntity{
    public DigitalTransitRecordBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DIGITAL_TRANSIT_RECORD, pos, blockState);
    }

    @Override
    public boolean shouldRender() {
        return true;
    }
}
