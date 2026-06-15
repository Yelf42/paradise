package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WarningLight extends BaseEntityBlock {

    public static final MapCodec<WarningLight> CODEC = simpleCodec(WarningLight::new);
    public static final Property<Boolean> POWERED = BlockStateProperties.POWERED;

    public static final Property<Direction> FACING = BlockStateProperties.FACING;

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Shapes.box(3/16.0, 0.0,   3/16.0,  13/16.0, 0.125,  13/16.0),
            Shapes.box(0.25,   0.125, 0.25,    0.75,    0.6875, 0.75)
    );

    private static final VoxelShape UP_AABB = Shapes.or(
            Shapes.box(3/16.0, 0.0,    3/16.0,  13/16.0, 0.125,  13/16.0),
            Shapes.box(0.25,   0.125,  0.25,    0.75,    0.6875, 0.75)
    );
    private static final VoxelShape DOWN_AABB = Shapes.or(
            Shapes.box(3/16.0, 0.875,  3/16.0,  13/16.0, 1.0,    13/16.0),
            Shapes.box(0.25,   0.3125, 0.25,    0.75,    0.875,  0.75)
    );
    private static final VoxelShape NORTH_AABB = Shapes.or(
            Shapes.box(3/16.0, 3/16.0, 0.875,   13/16.0, 13/16.0, 1.0),
            Shapes.box(0.25,   0.25,   0.3125,  0.75,    0.75,    0.875)
    );
    private static final VoxelShape SOUTH_AABB = Shapes.or(
            Shapes.box(3/16.0, 3/16.0, 0.0,     13/16.0, 13/16.0, 0.125),
            Shapes.box(0.25,   0.25,   0.125,   0.75,    0.75,    0.6875)
    );
    private static final VoxelShape EAST_AABB = Shapes.or(
            Shapes.box(0.0,    3/16.0, 3/16.0,  0.125,   13/16.0, 13/16.0),
            Shapes.box(0.125,  0.25,   0.25,    0.6875,  0.75,    0.75)
    );
    private static final VoxelShape WEST_AABB = Shapes.or(
            Shapes.box(0.875,  3/16.0, 3/16.0,  1.0,     13/16.0, 13/16.0),
            Shapes.box(0.3125, 0.25,   0.25,    0.875,   0.75,    0.75)
    );


    public WarningLight(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    public MapCodec<WarningLight> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case DOWN -> DOWN_AABB;
            default -> UP_AABB;
        };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        return level.getBlockState(blockpos).isFaceSturdy(level, blockpos, direction);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())).setValue(FACING, context.getClickedFace());
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, state.cycle(POWERED), 2);
                    WarningLightBlockEntity digitalWarningLightBlockEntity = (WarningLightBlockEntity) serverLevel.getBlockEntity(pos);
                    if (digitalWarningLightBlockEntity != null) digitalWarningLightBlockEntity.setPowered(true);
                }
            }
        }
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
            WarningLightBlockEntity digitalWarningLightBlockEntity = (WarningLightBlockEntity) level.getBlockEntity(pos);
            if (digitalWarningLightBlockEntity != null) digitalWarningLightBlockEntity.setPowered(false);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WarningLightBlockEntity(blockPos, blockState);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
    }
}
