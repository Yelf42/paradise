package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        boolean digital = context.getLevel().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital) return null;
        BlockPos pos = context.getClickedPos();
        return ModBlocks.DIGITAL_GRASS.defaultBlockState().setValue(OFFSET, 7 - Math.floorMod(pos.getZ(), 8));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.setBlockAndUpdate(pos, state.setValue(OFFSET, 7 - Math.floorMod(pos.getZ(), 8)));
        }
    }
}
