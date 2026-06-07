package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.yelf42.paradise.blocks.WarningLight.FACING;
import static com.yelf42.paradise.blocks.WarningLight.POWERED;

public class WarningLightBlockEntity extends BlockEntity {

    private boolean powered;
    private Direction facing;

    public WarningLightBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WARNING_LIGHT, pos, blockState);
        this.powered = blockState.getValue(POWERED);
        this.facing = blockState.getValue(FACING);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.powered = tag.getBoolean("powered");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("powered", powered);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    public Direction getFacing() {
        return facing;
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }
}
