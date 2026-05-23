package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.dimensions.WhitelistsSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DigitalWhitelister extends BaseEntityBlock {

    public static final MapCodec<DigitalWhitelister> CODEC = simpleCodec(DigitalWhitelister::new);
    protected static final VoxelShape SHAPE = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 2.0F, 16.0F);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final AABB AABB = new AABB(-2, -2, -2, 3, 3, 3);

    public DigitalWhitelister(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    public MapCodec<DigitalWhitelister> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DigitalWhitelisterBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean digital = context.getLevel().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital) return null;
        return super.getStateForPlacement(context);
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockPos = pos.below();
        return this.canSurviveOn(level, blockPos, level.getBlockState(blockPos));
    }

    protected boolean canSurviveOn(LevelReader level, BlockPos pos, BlockState state) {
        return state.isFaceSturdy(level, pos, Direction.UP, SupportType.RIGID);
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.canSurvive(level, pos)) {
            boolean flag = level.hasNeighborSignal(pos);
            if (flag != state.getValue(POWERED)) {
                if (flag) {
                    // TODO unlock sfx?
                    if (level instanceof ServerLevel serverLevel) tryReactivate(serverLevel, pos);
                }
                level.setBlock(pos, state.setValue(POWERED, flag), 3);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof DigitalWhitelisterBlockEntity digitalWhitelisterBlockEntity) {
                    digitalWhitelisterBlockEntity.setPowered(flag);
                }
            }
        } else {
            BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            dropResources(state, level, pos, blockEntity);
            level.removeBlock(pos, false);
        }
    }

    private void tryReactivate(ServerLevel level, BlockPos pos) {
        WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(level.getServer().overworld());
        ResourceLocation dimId = level.dimension().location();
        IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(level);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, AABB.move(pos))) {
            if (whitelistsSavedData.reactivatePlayer(dimId, player.getName().getString())) {
                intrudersSavedData.remove(player.getUUID());
                addParticlesAroundPlayer(ParticleTypes.HAPPY_VILLAGER, level, player);
            }
        }
    }

    private void addParticlesAroundPlayer(ParticleOptions particleOption, ServerLevel level, ServerPlayer player) {
        level.sendParticles(player, particleOption, false, player.getX(), player.getY() + 0.7, player.getZ(), 12, 0.5f, 0.5f, 0.5f, 0.2f);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
