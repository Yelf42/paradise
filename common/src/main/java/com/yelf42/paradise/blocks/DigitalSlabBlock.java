package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DigitalSlabBlock extends SlabBlock {
    public DigitalSlabBlock(Properties properties) {
        super(properties);
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean digital = context.getLevel().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital) return null;
        return super.getStateForPlacement(context);
    }
}
