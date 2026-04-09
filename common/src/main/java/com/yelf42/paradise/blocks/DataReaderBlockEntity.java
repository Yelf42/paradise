package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModComponents;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

import java.util.List;

public class DataReaderBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {

    private ItemStack item = new ItemStack(Items.AIR);
    private boolean cooldown = false;
    private boolean displayPortal = false;

    private static final AABB LOCAL_AABB = new AABB(0, 0, 0, 1, 3, 1).contract(0.2, 0, 0.2);

    public DataReaderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DATA_READER, pos, blockState);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.getString("dimension").equals("NULL")) {
            setTheItem(new ItemStack(Items.AIR));
        } else {
            ItemStack disc = new ItemStack(ModItems.ACCESS_DISC);
            disc.set(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(ResourceLocation.tryParse(tag.getString("dimension"))));
            setTheItem(disc);
        }

        this.cooldown = tag.getBoolean("cooldown");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("cooldown", this.cooldown);
        if (this.item == null || isEmpty()) {
            tag.putString("dimension", "NULL");
        } else {
            String id = this.item.getOrDefault(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(Paradise.identifier(""))).address().toString();
            tag.putString("dimension", id);
        }

    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public void setDisplayPortal(boolean state) {
        this.displayPortal = state;
    }

    public boolean shouldRenderPortal() {
        return this.displayPortal;
    }

    @Override
    public ItemStack getTheItem() {
        return item;
    }

    public ResourceLocation getDimension() {
        return this.item.getOrDefault(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(Paradise.identifier(""))).address();
    }

    @Override
    public void setTheItem(ItemStack itemStack) {
        this.item = itemStack;
    }

    public boolean getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(boolean state) {
        if (!state && !this.isEmpty() && invalidItem()) popDisc();
        this.cooldown = state;
    }

    private boolean invalidItem() {
        return !this.item.has(ModComponents.DIMENSION_ADDRESS) || this.item.getOrDefault(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(Paradise.identifier(""))).address().getPath().isEmpty();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DataReaderBlockEntity dataReader) {
        int hasDisc = state.getValue(DataReaderBlock.HAS_DISC);
        dataReader.setDisplayPortal(hasDisc == 2);
        if (dataReader.getCooldown() || hasDisc == 0) return;

        if (!level.isClientSide) {
            if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir()) {
                if (hasDisc == 2) {
                    level.setBlock(pos, state.setValue(DataReaderBlock.HAS_DISC, 1), 3);
                }
                return;
            } else if ((hasDisc == 1) && !dataReader.cooldown && !dataReader.invalidItem()) {
                level.setBlock(pos, state.setValue(DataReaderBlock.HAS_DISC, 2), 3);
                return;
            }
        }
    }

    public void popDisc() {
        // TODO disc removal sfx
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            BlockState reader = this.level.getBlockState(blockpos);
            Direction facing = reader.getValue(DataReaderBlock.FACING);
            if (!itemstack.isEmpty()) {
                ItemStack copy = itemstack.copy();
                spawnItem(this.level, copy, 0.1, facing, getDispensePosition(blockpos.getCenter(), facing));
                this.removeTheItem();
            }
        }
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
}
