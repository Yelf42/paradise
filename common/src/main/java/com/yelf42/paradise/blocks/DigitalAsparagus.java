package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

// TODO assets
// TODO texture shifting like bamboo?
public class DigitalAsparagus extends Block implements SimpleWaterloggedBlock, BonemealableBlock {

    // 0 top
    // 1 mid
    // 2 bot
    public static final IntegerProperty SECTION = IntegerProperty.create("section", 0, 2);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape BODY_SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    public static final VoxelShape TOP_SHAPE = Block.box(4, 0, 4, 12, 12, 12);

    public DigitalAsparagus(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SECTION, 0).setValue(WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return (state.getValue(SECTION) == 0 ? TOP_SHAPE : BODY_SHAPE).move(offset.x(), offset.y(), offset.z());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.DIRT) || below.is(ModBlocks.DIGITAL_VOLUME) || below.is(ModBlocks.DIGITAL_ASPARAGUS);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        BlockState defaultState = super.getStateForPlacement(context).setValue(WATERLOGGED, flag);
        Level level = context.getLevel();
        boolean digital = level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital || defaultState == null) return null;

        BlockPos pos = context.getClickedPos();
        boolean asparagusBelow = level.getBlockState(pos.below()).is(ModBlocks.DIGITAL_ASPARAGUS);
        boolean asparagusAbove = level.getBlockState(pos.above()).is(ModBlocks.DIGITAL_ASPARAGUS);
        return defaultState.setValue(SECTION, asparagusAbove ? (asparagusBelow ? 1 : 2) : 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SECTION, WATERLOGGED);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (!state.canSurvive(level, currentPos)) return Blocks.AIR.defaultBlockState();

        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        boolean asparagusBelow = level.getBlockState(currentPos.below()).is(ModBlocks.DIGITAL_ASPARAGUS);
        boolean asparagusAbove = level.getBlockState(currentPos.above()).is(ModBlocks.DIGITAL_ASPARAGUS);
        return state.setValue(SECTION, asparagusAbove ? (asparagusBelow ? 1 : 2) : 0);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        BlockState above = levelReader.getBlockState(blockPos.above());
        return above.isAir() || above.is(Blocks.WATER);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return randomSource.nextInt(4) == 0;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        FluidState fluidstate = serverLevel.getFluidState(blockPos.above());
        boolean waterlogged = fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() >= 6;
        serverLevel.setBlock(blockPos.above(), ModBlocks.DIGITAL_ASPARAGUS.defaultBlockState().setValue(SECTION, 0).setValue(WATERLOGGED, waterlogged), 3);
    }
}
