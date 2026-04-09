package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DownloaderLocations;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DigitalUploaderBlock extends BaseEntityBlock implements Portal {

    public static final MapCodec<DigitalUploaderBlock> CODEC = simpleCodec(DigitalUploaderBlock::new);
    protected static final VoxelShape AABB = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 8.0F, 16.0F);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public DigitalUploaderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean digital = context.getLevel().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital) return null;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DigitalUploaderBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.DIGITAL_UPLOADER, DigitalUploaderBlockEntity::tick);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack itemstack = player.getItemInHand(hand);
        DigitalUploaderBlockEntity dube = (DigitalUploaderBlockEntity) level.getBlockEntity(pos);
        if (dube == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (dube.hasItem()) {
            dube.popItem();
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            if (itemstack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            dube.setTheItem(itemstack.copyWithCount(1));
            itemstack.shrink(1);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 40;
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
        DigitalUploaderBlockEntity dube = (DigitalUploaderBlockEntity) serverLevel.getBlockEntity(blockPos);
        if (dube == null || !dube.shouldRender()) return null;

        DownloaderLocations downloaders = DownloaderLocations.getOrCreate(serverLevel.getServer().overworld());
        Pair<BlockPos, ResourceLocation> destination = downloaders.get(dube.getAddress());
        if (destination == null) return null;

        BlockPos serverLocation = destination.getLeft();
        if (serverLocation == null) return null;

        ServerLevel serverlevel = serverLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, destination.getRight()));
        if (serverlevel == null) return null;

        ChunkPos chunkPos = new ChunkPos(serverLocation);
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, true);

        // Check if actually downloader position
        BlockState downloader = serverlevel.getBlockState(serverLocation);
        if (!downloader.is(ModBlocks.DATA_DOWNLOADER)) {
            serverlevel.setChunkForced(chunkPos.x, chunkPos.z, false);
            downloaders.remove(dube.getAddress());
            return null;
        }

        if (!downloader.getOptionalValue(DataDownloaderBlock.POWERED).orElse(true)) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("gui.paradise.uploader.unavailable").withStyle(ChatFormatting.RED), true);
            }
            return null;
        }

        // Clear target location:
        for (int i = 1; i <= 2; i++) {
            BlockPos target = serverLocation.above(i);
            Block.dropResources(serverlevel.getBlockState(target), serverlevel, target, serverlevel.getBlockEntity(target));
            serverlevel.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        }
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, false);

        float f = downloader.getOptionalValue(DataDownloaderBlock.FACING).orElse(Direction.NORTH).toYRot();
        Vec3 vec3 = serverLocation.above().getBottomCenter();
        return new DimensionTransition(serverlevel, vec3, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }

    @Override
    public Transition getLocalTransition() {
        return Transition.CONFUSION;
    }
}
