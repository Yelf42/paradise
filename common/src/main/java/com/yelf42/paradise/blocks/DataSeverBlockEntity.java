package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DataServerLocations;
import com.yelf42.paradise.dimensions.DimensionProvider;
import com.yelf42.paradise.dimensions.DimensionRegistry;
import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DataSeverBlockEntity extends BlockEntity {

    private ResourceLocation dimension;
    private boolean cooldown = false;

    public DataSeverBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DATA_SERVER, pos, blockState);
        this.dimension = Paradise.identifier("");
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        this.cooldown = tag.getBoolean("cooldown");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("dimension",this.dimension.toString());
        tag.putBoolean("cooldown", this.cooldown);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (!level.isClientSide) {
            DimensionRegistry.ParadiseType type = DimensionRegistry.ParadiseType.DAY;
            if (level.getRandom().nextInt(4)  == 0) type = DimensionRegistry.ParadiseType.NIGHT;
            if (level.getRandom().nextInt(20) == 0) type = DimensionRegistry.ParadiseType.ERROR;

            if (this.dimension.getPath().isEmpty()) {
                this.dimension = ((DimensionProvider) level.getServer()).paradise$createIfAbsent(type);
                //Paradise.LOGGER.info("Created dimension: " + dimension + ", at: " + this.worldPosition);
            }

            DataServerLocations dsl = DataServerLocations.getOrCreate(level.getServer().overworld());
            dsl.add((type == DimensionRegistry.ParadiseType.ERROR) ? Paradise.identifier("nullspace") : this.dimension, this.getBlockPos(), level.dimension().location());
        }
    }

    public ResourceLocation getDimension() {
        return this.dimension;
    }

    public boolean offCooldown() {
        return !this.cooldown;
    }

    public void setCooldown(boolean state) {
        this.cooldown = state;
    }
}
