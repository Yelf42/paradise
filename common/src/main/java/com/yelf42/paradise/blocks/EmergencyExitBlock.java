package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DataServerLocations;
import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class EmergencyExitBlock extends DigitalUploaderBlock {
    public static final MapCodec<EmergencyExitBlock> CODEC = simpleCodec(EmergencyExitBlock::new);

    public EmergencyExitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EmergencyExitBlockEntity(blockPos, blockState);
    }
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {return null;}

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 40;
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos blockPos) {
        DataServerLocations dsl = DataServerLocations.getOrCreate(level.getServer().overworld());
        ResourceLocation dim = level.dimension().location();

        Pair<BlockPos, ResourceLocation> destination = dsl.get(dim);
        if (destination == null
                || destination.getLeft() == null
                || destination.getRight() == null
                || level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, destination.getRight())) == null) {

            if (entity instanceof ServerPlayer serverplayer) {
                return new DimensionTransition(serverplayer.server.overworld(), serverplayer, DimensionTransition.DO_NOTHING);
            }
            return null;
        }

        BlockPos serverLocation = destination.getLeft();
        ServerLevel serverlevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, destination.getRight()));

        // Clear target location:
        ChunkPos chunkPos = new ChunkPos(serverLocation);
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, true);
        for (int i = 0; i <= 1; i++) {
            BlockPos target = serverLocation.north().above(i);
            Block.dropResources(serverlevel.getBlockState(target), serverlevel, target, serverlevel.getBlockEntity(target));
            serverlevel.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        }
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, false);


        Vec3 vec3 = serverLocation.north().getBottomCenter();
        float f = Direction.NORTH.toYRot();
        return new DimensionTransition(serverlevel, vec3, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Transition.CONFUSION;
    }
}
