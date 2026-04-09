package com.yelf42.paradise.blocks;

import com.yelf42.paradise.dimensions.DownloaderLocations;
import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;

public class DigitalUploaderBlockEntity extends AbstractDigitalSymbolBlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {

    private ItemStack addressItem= ItemStack.EMPTY;
    private boolean validAddress = false;

    public DigitalUploaderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DIGITAL_UPLOADER, pos, blockState);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("address_item")) {
            this.setTheItem(ItemStack.parse(registries, tag.getCompound("address_item")).orElse(ItemStack.EMPTY));
        } else {
            this.setTheItem(ItemStack.EMPTY);
        }
        this.validAddress = tag.getBoolean("valid_address");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.addressItem.isEmpty()) tag.put("address_item", addressItem.save(registries));
        tag.putBoolean("valid_address", validAddress);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DigitalUploaderBlockEntity uploader) {
        if (level.getGameTime() % 60 == 0) {
            if (!level.isClientSide) {
                boolean prev = uploader.validAddress;
                boolean curr  = uploader.isAddressValid(level);
                if (prev != curr) {
                    uploader.validAddress = curr;
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }

        }
    }

    private boolean isAddressValid(Level level) {
        if (level.getServer().overworld() == null || this.addressItem.isEmpty()) return false;
        DownloaderLocations downloaders = DownloaderLocations.getOrCreate(level.getServer().overworld());
        return downloaders.has(this.addressItem.getHoverName().getString());
    }

    @Override
    public boolean shouldRender() {
        return this.validAddress;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public ItemStack getTheItem() {
        return this.addressItem;
    }

    public boolean hasItem() {
        return !this.addressItem.isEmpty();
    }

    public String getAddress() {
        if (hasItem()) return this.addressItem.getHoverName().getString();
        return "ERROR";
    }

    @Override
    public void setTheItem(ItemStack itemStack) {
        this.addressItem = itemStack;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.validAddress = this.isAddressValid(level);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
    }

    public void popItem() {
        // TODO item removal sfx
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            if (!itemstack.isEmpty()) {
                ItemStack copy = itemstack.copy();
                spawnItem(this.level, copy, blockpos.getCenter());
            }
        }
        this.setTheItem(ItemStack.EMPTY);
    }

    private static void spawnItem(Level level, ItemStack stack, Position position) {
        double d0 = position.x();
        double d1 = position.y();
        double d2 = position.z();

        ItemEntity itementity = new ItemEntity(level, d0, d1, d2, stack);
        itementity.setDeltaMovement(0.0, 0.08, 0.0);
        level.addFreshEntity(itementity);
    }
}
