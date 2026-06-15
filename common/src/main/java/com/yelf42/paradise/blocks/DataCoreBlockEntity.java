package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DataCoreBlockEntity extends BlockEntity {

    private long soundStartTime = -1;

    public DataCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DATA_CORE, pos, blockState);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DataCoreBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.soundStartTime == -1) {
            be.soundStartTime = level.getGameTime();
        }

        long elapsed = level.getGameTime() - be.soundStartTime;
        if (elapsed % 40 == 0) {
            Vec3 pos = blockPos.getCenter();
            level.playSound(null, pos.x(), pos.y(), pos.z(), ModSounds.DATA_CORE_HUM, SoundSource.BLOCKS);
        }
    }


}
