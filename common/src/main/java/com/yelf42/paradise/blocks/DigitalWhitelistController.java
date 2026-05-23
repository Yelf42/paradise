package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.dimensions.WhitelistsSavedData;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModPackets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DigitalWhitelistController extends BaseEntityBlock {

    public static final MapCodec<DigitalWhitelistController> CODEC = simpleCodec(DigitalWhitelistController::new);

    public DigitalWhitelistController(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DigitalWhitelistControllerBlockEntity(blockPos, blockState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.WHITELIST_CONTROLLER, DigitalWhitelistControllerBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (hitResult.getDirection() != Direction.WEST) return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DigitalWhitelistControllerBlockEntity controller) {
            if (!this.otherPlayerIsEditingSign(player, controller) && player.mayBuild()) {
                if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
                    if (!openEditor(serverLevel, serverPlayer, controller, pos)) {
                        player.displayClientMessage(Component.translatable("gui.paradise.teleport.not_whitelisted").withStyle(ChatFormatting.RED), true);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public boolean openEditor(ServerLevel level, ServerPlayer serverPlayer, DigitalWhitelistControllerBlockEntity controller, BlockPos pos) {
        controller.setAllowedPlayerEditor(serverPlayer.getUUID());
        WhitelistsSavedData data = WhitelistsSavedData.getOrCreate(level.getServer().overworld());
        ResourceLocation dimId = serverPlayer.serverLevel().dimension().location();

        if (data.hasWhitelist(dimId) && (data.isWhitelisted(dimId, serverPlayer.getName().getString()) || serverPlayer.isCreative())) {
            if (!serverPlayer.isCreative()) data.addPlayer(dimId, serverPlayer.getName().getString());
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(new ModPackets.OpenWhitelistPayload(
                    dimId,
                    data.getActive(dimId),
                    data.getHistory(dimId),
                    pos
            ));
            serverPlayer.connection.send(packet);
            return true;
        }
        return false;
    }

    private boolean otherPlayerIsEditingSign(Player player, DigitalWhitelistControllerBlockEntity controller) {
        UUID uuid = controller.getPlayerWhoMayEdit();
        return uuid != null && !uuid.equals(player.getUUID());
    }
}
