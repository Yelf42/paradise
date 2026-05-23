package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class DigitalWhitelistControllerBlockEntity extends BlockEntity {
    // Nullable
    private UUID playerWhoMayEdit;

    public DigitalWhitelistControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WHITELIST_CONTROLLER, pos, blockState);
    }

    public void setAllowedPlayerEditor(UUID playWhoMayEdit) {
        this.playerWhoMayEdit = playWhoMayEdit;
    }

    // Nullable
    public UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    public boolean playerIsTooFarAwayToEdit(UUID uuid) {
        Player player = this.level.getPlayerByUUID(uuid);
        return player == null || !player.canInteractWithBlock(this.getBlockPos(), 4.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DigitalWhitelistControllerBlockEntity controller) {
        UUID uuid = controller.getPlayerWhoMayEdit();
        if (uuid != null) {
            controller.clearInvalidPlayerWhoMayEdit(controller, level, uuid);
        }
    }

    private void clearInvalidPlayerWhoMayEdit(DigitalWhitelistControllerBlockEntity controller, Level level, UUID uuid) {
        if (controller.playerIsTooFarAwayToEdit(uuid)) {
            controller.setAllowedPlayerEditor(null);
        }
    }
}
