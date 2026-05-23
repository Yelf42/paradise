package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;


public class DigitalWhitelisterBlockEntity extends AbstractDigitalSymbolBlockEntity{

    private boolean powered = false;

    public DigitalWhitelisterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DIGITAL_WHITELISTER, pos, blockState);
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.powered = tag.getBoolean("powered");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("powered", this.powered);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
