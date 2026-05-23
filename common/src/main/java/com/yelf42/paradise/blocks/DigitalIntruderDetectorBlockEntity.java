package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.yelf42.paradise.blocks.DigitalIntruderDetector.DETECTED;
import static net.minecraft.world.level.block.Block.UPDATE_ALL;

public class DigitalIntruderDetectorBlockEntity extends AbstractDigitalSymbolBlockEntity {

    private boolean detected = false;

    public DigitalIntruderDetectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DIGITAL_INTRUDER_DETECTOR, pos, blockState);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.detected = tag.getBoolean("detected");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("detected", detected);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DigitalIntruderDetectorBlockEntity detector) {
        if (level.getGameTime() % 20 == 0) {
            if (level instanceof ServerLevel serverLevel) {
                IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(serverLevel);
                boolean currentDetection = state.getValue(DETECTED);
                if (intrudersSavedData.intrudersPresent(serverLevel)) {
                    if (!currentDetection) {
                        level.setBlock(pos, state.setValue(DETECTED, true), UPDATE_ALL);
                        detector.detected = true;
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                } else {
                    if (currentDetection) {
                        level.setBlock(pos, state.setValue(DETECTED, false), UPDATE_ALL);
                        detector.detected = false;
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldRender() {
        return detected;
    }
}
