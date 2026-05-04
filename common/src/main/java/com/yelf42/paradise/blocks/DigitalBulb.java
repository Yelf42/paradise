package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

// TODO emissive filament
public class DigitalBulb extends Block {

    public static final MapCodec<DigitalBulb> CODEC = simpleCodec(DigitalBulb::new);

    public static final VoxelShape AABB = Shapes.box(0.35, 0.3, 0.35, 0.65, 0.6, 0.65);

    public static final BooleanProperty BULB = BooleanProperty.create("bulb");
    public static final BooleanProperty CHAINED = BooleanProperty.create("chained");

    public DigitalBulb(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BULB, true).setValue(CHAINED, false));
    }

    public MapCodec<DigitalBulb> codec() {
        return CODEC;
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(BULB) || level.getBlockState(pos.below()).is(ModBlocks.DIGITAL_BULB);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState defaultState = super.getStateForPlacement(context);
        Level level = context.getLevel();
        boolean digital = level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital || defaultState == null) return null;
        boolean chained = context.getLevel().getBlockState(context.getClickedPos().below()).is(ModBlocks.DIGITAL_BULB);
        return defaultState.setValue(CHAINED, chained);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = state.getOffset(level, pos);
        return state.getValue(BULB) ? AABB.move(vec3.x, vec3.y, vec3.z) : Shapes.empty();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return super.canBeReplaced(state, useContext) || !state.getValue(BULB);
    }

    @Override
    protected void tick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (serverLevel.getBlockState(above).isAir() && above.getY() < serverLevel.getMaxBuildHeight()) {
            serverLevel.setBlock(above, this.defaultBlockState().setValue(BULB, false), 3);
        }
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (neighborPos.distManhattan(pos.above()) == 0 && neighborState.isAir()) {
            level.scheduleTick(pos, this, 1, TickPriority.HIGH);
        }

        boolean shouldChain = state.getValue(BULB) && level.getBlockState(pos.below()).is(ModBlocks.DIGITAL_BULB);
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state.setValue(CHAINED, shouldChain), direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos above = pos.above();
            if (serverLevel.getBlockState(above).isAir() && above.getY() < serverLevel.getMaxBuildHeight()) {
                serverLevel.setBlock(above, this.defaultBlockState().setValue(BULB, false), 3);
            }
        }

        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        if (!state.getValue(BULB)) return SoundType.EMPTY;
        return SoundType.LANTERN;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BULB, CHAINED);
    }

}
