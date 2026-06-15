package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.dimensions.DownloaderLocations;
import com.yelf42.paradise.registry.ModComponents;
import com.yelf42.paradise.registry.ModItems;
import com.yelf42.paradise.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

// TODO Create compatibility?
public class DataDownloaderBlock extends BaseEntityBlock {

    public static final MapCodec<DataDownloaderBlock> CODEC = simpleCodec(DataDownloaderBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty PRINTING = BooleanProperty.create("printing");

    public DataDownloaderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(PRINTING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DataDownloaderBlockEntity(blockPos, blockState);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(PRINTING, false), 3);
        ItemStack stack = new ItemStack(ModItems.ADDRESS_CHIP);
        stack.set(ModComponents.DOWNLOADER_ADDRESS, new ModComponents.DownloaderAddressComponent(hashAddress(pos, level.dimension().location())));
        spawnItem(level, stack, 0.1, state.getValue(FACING), getDispensePosition(pos.getCenter(), state.getValue(FACING)));
    }

    private static void spawnItem(Level level, ItemStack stack, double speed, Direction facing, Position position) {
        double d0 = position.x();
        double d1 = position.y() - (double)0.15625F;
        double d2 = position.z();

        ItemEntity itementity = new ItemEntity(level, d0, d1, d2, stack);
        double d3 = level.random.nextDouble() * 0.1 + 0.05;
        itementity.setDeltaMovement(level.random.triangle((double)facing.getStepX() * d3, 0.017 * speed), 0.08, level.random.triangle((double)facing.getStepZ() * d3, 0.017 * speed));
        level.addFreshEntity(itementity);
    }

    public static Position getDispensePosition(Vec3 center, Direction direction) {
        return center.add(0.7 * (double)direction.getStepX(), 0.7 * (double)direction.getStepY(), 0.7 * (double)direction.getStepZ());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (state.getValue(PRINTING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack inHand = player.getItemInHand(hand);
        if (inHand.is(ModItems.ADDRESS_CHIP)) {
            if (!level.isClientSide) {
                inHand.shrink(1);
                level.setBlock(pos, state.setValue(PRINTING, true), 3);
                level.playSound(null, pos.getCenter().x(), pos.getCenter().y(), pos.getCenter().z(), ModSounds.DATA_WRITE, SoundSource.BLOCKS);
                level.scheduleTick(pos, this, 80, TickPriority.NORMAL);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        if (blockPos.getY() >= level.getMaxBuildHeight() - 2) return null;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                level.setBlock(pos, state.cycle(POWERED), 2);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) {
            DownloaderLocations downloaders = DownloaderLocations.getOrCreate(serverLevel.getServer().overworld());
            downloaders.add(hashAddress(pos, level.dimension().location()), pos, level.dimension().location());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            DownloaderLocations downloaders = DownloaderLocations.getOrCreate(level.getServer().overworld());
            downloaders.remove(hashAddress(pos, level.dimension().location()));
            if (state.getValue(PRINTING)) {
                ItemStack stack = new ItemStack(ModItems.ADDRESS_CHIP);
                spawnItem(level, stack, 0.1, state.getValue(FACING), getDispensePosition(pos.getCenter(), state.getValue(FACING)));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private String hashAddress(BlockPos pos, ResourceLocation dim) {
        return dim.toString() + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, PRINTING);
    }
}
