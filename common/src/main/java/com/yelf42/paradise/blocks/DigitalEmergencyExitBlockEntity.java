package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DigitalEmergencyExitBlockEntity extends AbstractDigitalSymbolBlockEntity {
    public DigitalEmergencyExitBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.EMERGENCY_EXIT, pos, blockState);
    }

    @Override
    public boolean shouldRender() {
        return true;
    }
}
