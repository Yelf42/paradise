package com.yelf42.paradise.blocks;

import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DigitalEmergencyExitBlockEntity extends AbstractDigitalSymbolBlockEntity {
    public DigitalEmergencyExitBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.EMERGENCY_EXIT, pos, blockState);
    }



    @Override
    public boolean shouldRender() {
        return true;
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DigitalEmergencyExitBlockEntity digitalEmergencyExitBlockEntity) {
        if (level.isClientSide()) {
            if (!digitalEmergencyExitBlockEntity.shouldRender() || level.getRandom().nextInt(5) != 0) return;
            Vec3 p = blockPos.getBottomCenter();
            level.addParticle(ModParticles.ASCENDING_BITS, p.x() + 0.35 - level.getRandom().nextDouble() * 0.7, p.y() + 0.52, p.z() + 0.35 - level.getRandom().nextDouble() * 0.7, 0, level.getRandom().nextDouble() * 0.03 + 0.02, 0);
        }
    }
}
