package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

// TODO emissive top when updated to 1.21.11+
public class DataReaderBlock extends BaseEntityBlock implements Portal {

    public static final MapCodec<DataReaderBlock> CODEC = simpleCodec(DataReaderBlock::new);

    // 0 = empty, 1 = loading, 2 = full
    public static final IntegerProperty HAS_DISC = IntegerProperty.create("has_disc", 0, 2);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public DataReaderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_DISC, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        if (blockPos.getY() >= level.getMaxBuildHeight() - 2) return null;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DataReaderBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.DATA_READER, DataReaderBlockEntity::tick);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        DataReaderBlockEntity drbe = level.getBlockEntity(pos, ModBlockEntities.DATA_READER).orElse(null);
        if (drbe == null) return;
        drbe.setCooldown(false);
        if (!drbe.isEmpty()) {
            level.setBlock(pos, state.setValue(HAS_DISC, 2), 3);
        } else {
            level.setBlock(pos, state.setValue(HAS_DISC, 0), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        DataReaderBlockEntity drbe = level.getBlockEntity(pos, ModBlockEntities.DATA_READER).orElse(null);
        if (drbe == null || drbe.getCooldown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack itemstack = player.getItemInHand(hand);
        if (state.getValue(HAS_DISC) > 0) {
            if (itemstack.isEmpty()) {
                // Pop disc
                level.setBlock(pos, state.setValue(HAS_DISC, 0), 3);
                drbe.setCooldown(false);
                drbe.popDisc();
                level.scheduleTick(pos, state.getBlock(), 100, TickPriority.NORMAL);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
            if (itemstack.is(ModItems.ACCESS_DISC)) {
                // Insert disc
                level.setBlock(pos, state.setValue(HAS_DISC, 1), 3);
                ItemStack disc = itemstack.copy();
                itemstack.shrink(1);
                drbe.setTheItem(disc);
                drbe.setCooldown(true);                // TODO play CD insert sound, change tick delay to sound length
                level.scheduleTick(pos, state.getBlock(), 200, TickPriority.NORMAL);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(HAS_DISC) * 7;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_DISC, FACING);
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 40;
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos blockPos) {
        DataReaderBlockEntity drbe = level.getBlockEntity(blockPos, ModBlockEntities.DATA_READER).orElse(null);
        if (drbe == null || drbe.getCooldown()) return null;

        ServerLevel serverlevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, drbe.getDimension()));
        if (serverlevel == null) return null;

        // Clear target location:
        BlockPos spawnPos = new BlockPos(56, 6, 0);
        ChunkPos chunkPos = new ChunkPos(spawnPos);
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, true);
        for (int i = 0; i <= 1; i++) {
            BlockPos target = spawnPos.above(i);
            Block.dropResources(serverlevel.getBlockState(target), serverlevel, target, serverlevel.getBlockEntity(target));
            serverlevel.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        }
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, false);

        Vec3 vec3 = new Vec3(56.5, 6, 0.5);
        float f = Direction.WEST.toYRot();
        return new DimensionTransition(serverlevel, vec3, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }

    // TODO replace with digital-y shader
    @Override
    public Transition getLocalTransition() {
        return Transition.CONFUSION;
    }
}
