package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

public class DigitalGrassSlabBlock extends DigitalSlabBlock {

    public static final MapCodec<DigitalGrassSlabBlock> CODEC = simpleCodec(DigitalGrassSlabBlock::new);
    public static final IntegerProperty OFFSET = IntegerProperty.create("offset", 0, 7);

    public DigitalGrassSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OFFSET, 0).setValue(WATERLOGGED, false).setValue(TYPE, SlabType.BOTTOM));
    }

    public MapCodec<DigitalGrassSlabBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OFFSET);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState defaultState = super.getStateForPlacement(context);
        Level level = context.getLevel();
        if (defaultState == null) return null;
        BlockPos pos = context.getClickedPos();
        return defaultState.setValue(OFFSET, DigitalGrassBlock.getOffsetForPos(pos, level.dimension().location().getPath()));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.setBlockAndUpdate(pos, state.setValue(OFFSET, DigitalGrassBlock.getOffsetForPos(pos, level.dimension().location().getPath())));
        }
    }
}
