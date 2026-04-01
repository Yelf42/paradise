package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DataReaderBlock extends BaseEntityBlock {

    public static final MapCodec<DataReaderBlock> CODEC = simpleCodec(DataReaderBlock::new);

    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;;

    public DataReaderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_DISC, false).setValue(ON, false).setValue(FACING, Direction.NORTH));
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
        return null; // TODO
        // TODO block entity turn off reader if blocks where portal should be
    }


    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack itemstack = player.getItemInHand(hand);
        if (state.getValue(HAS_DISC)) {
            if (itemstack.isEmpty()) {
                // Pop disc
                level.setBlock(pos, state.setValue(HAS_DISC, false), 3);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
            if (itemstack.is(Items.MUSIC_DISC_OTHERSIDE)) {
                // Insert disc
                level.setBlock(pos, state.setValue(HAS_DISC, true), 3);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level instanceof ServerLevel serverlevel) {
            this.checkAndFlip(state, serverlevel, pos);
        }
    }

    public void checkAndFlip(BlockState state, ServerLevel level, BlockPos pos) {
        boolean flag = level.hasNeighborSignal(pos);
        if (flag != state.getValue(POWERED)) {
            BlockState blockstate = state;
            if (!(Boolean)state.getValue(POWERED)) {
                if (state.getValue(ON)) {
                    blockstate = state.cycle(ON);
                } else {
                    blockstate = state.setValue(ON, level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above().above()).isAir());
                }
                level.playSound(null, pos, blockstate.getValue(ON) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
            }
            level.setBlock(pos, blockstate.setValue(POWERED, flag), 3);
        }
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
        int on = level.getBlockState(pos).getValue(ON) ? 7 : 0;
        int full = level.getBlockState(pos).getValue(HAS_DISC) ? 8 : 0;
        return on + full;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_DISC, ON, POWERED, FACING);
    }
}
