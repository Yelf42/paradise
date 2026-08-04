package com.yelf42.paradise.blocks;

import com.yelf42.paradise.dimensions.DownloaderLocations;
import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class DataDownloaderBlockEntity extends AbstractDigitalSymbolBlockEntity {

    private String address;

    public DataDownloaderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DATA_DOWNLOADER, pos, blockState);
    }

    @Override
    public boolean shouldRender() {
        return this.getBlockState().getValue(DataDownloaderBlock.POWERED);
    }

    public String getOrCreateAddress() {
        if (this.address == null) {
            this.address = UUID.randomUUID().toString();
            this.setChanged();
        }
        registerIfPossible();
        return this.address;
    }

    public String getAddressOrNull() {
        return this.address;
    }

    private void registerIfPossible() {
        if (this.address == null || this.level == null || this.level.isClientSide()) return;
        if (this.level instanceof ServerLevel serverLevel) {
            DownloaderLocations downloaders = DownloaderLocations.getOrCreate(serverLevel.getServer().overworld());
            downloaders.set(this.address, this.getBlockPos(), this.level.dimension().location());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("address")) {
            this.address = tag.getString("address");
        }
        registerIfPossible();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.address != null) {
            tag.putString("address", this.address);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        registerIfPossible();
    }
}
