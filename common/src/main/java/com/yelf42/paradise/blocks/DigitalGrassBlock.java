package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

// TODO transition from 7->0 issue
public class DigitalGrassBlock extends Block {

    public static final MapCodec<DigitalGrassBlock> CODEC = simpleCodec(DigitalGrassBlock::new);
    public static final IntegerProperty OFFSET = IntegerProperty.create("offset", 0, 7);

    public DigitalGrassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OFFSET, 0));
    }

    public MapCodec<DigitalGrassBlock> codec() {
        return CODEC;
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState defaultState = super.getStateForPlacement(context);
        Level level = context.getLevel();
        boolean digital = level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital || defaultState == null) return null;
        BlockPos pos = context.getClickedPos();
        return defaultState.setValue(OFFSET, getOffsetForPos(pos, level.dimension().location().getPath()));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.setBlockAndUpdate(pos, state.setValue(OFFSET, getOffsetForPos(pos, level.dimension().location().getPath())));
        }
    }

    // TODO Experiment with other noisy patterns
    public static int getOffsetForPos(BlockPos pos, String level) {
        int offset = level.hashCode();
        int noise = Math.toIntExact(Math.round((Paradise.PERLIN.getValue((offset + pos.getX()) * 0.05, (offset + pos.getZ()) * 0.05, false) + 1.0) * 4.0));
        return 7 - Math.floorMod(pos.getZ() - noise, 8);
    }
}
