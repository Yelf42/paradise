package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DataServerLocations;
import com.yelf42.paradise.dimensions.DimensionRegistry;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModComponents;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

// TODO ability to choose which DataServer is nullspace for specific structure
public class DataServerBlock extends BaseEntityBlock {

    public static final MapCodec<DataServerBlock> CODEC = simpleCodec(DataServerBlock::new);

    private static final BooleanProperty BURNING = BooleanProperty.create("burning");

    public DataServerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BURNING, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DataSeverBlockEntity(blockPos, blockState);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        DataSeverBlockEntity dsbe = level.getBlockEntity(pos, ModBlockEntities.DATA_SERVER).orElse(null);
        if (dsbe != null) {
            popAccessDisc(level, pos, dsbe.getDimension());
            dsbe.setCooldown(false);
            level.setBlock(pos, state.setValue(BURNING, false), 3);
        }
        super.tick(state, level, pos, random);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() != Direction.NORTH) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(Items.MUSIC_DISC_OTHERSIDE) || itemstack.is(ModItems.ACCESS_DISC)) {
            DataSeverBlockEntity dsbe = level.getBlockEntity(pos, ModBlockEntities.DATA_SERVER).orElse(null);
            if (dsbe != null && dsbe.offCooldown()) {
                itemstack.shrink(1);
                dsbe.setCooldown(true);
                level.setBlock(pos, state.setValue(BURNING, true), 3);
                // TODO play CD insert sound, change tick delay to sound length
                level.scheduleTick(pos, state.getBlock(), 100, TickPriority.NORMAL);
                return ItemInteractionResult.CONSUME;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void popAccessDisc(ServerLevel level, BlockPos pos, ResourceLocation id) {
        ItemStack toDrop = new ItemStack(ModItems.ACCESS_DISC);
        toDrop.set(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(id));
        spawnItem(level, toDrop, 0.1, Direction.NORTH, getDispensePosition(pos.getCenter(), Direction.NORTH));
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

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(BURNING) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BURNING);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.is(ModBlocks.DATA_SERVER) && !level.isClientSide()) {
            level.getBlockEntity(pos, ModBlockEntities.DATA_SERVER).ifPresent(dsbe -> {
                //Paradise.LOGGER.info("Trying to remove DataServer");
                DataServerLocations dsl = DataServerLocations.getOrCreate(level.getServer().overworld());
                dsl.remove(dsbe.getDimension(), null, null);
            });
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
