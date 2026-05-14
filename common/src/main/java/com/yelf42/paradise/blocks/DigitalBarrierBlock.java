package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class DigitalBarrierBlock extends Block {

    public static final MapCodec<DigitalBarrierBlock> CODEC = simpleCodec(DigitalBarrierBlock::new);

    public DigitalBarrierBlock(Properties properties) {
        super(properties);
    }

    public MapCodec<DigitalBarrierBlock> codec() {
        return CODEC;
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.9, 1.0);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);
        Vec3 position = entity.getPosition(0);

        if (level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) {
            level.addParticle(
                    ModParticles.DAY_RIPPLE,
                    position.x + (level.random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 1.01,
                    position.z + (level.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0
            );
        } else if (level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension_night")))) {
            level.addParticle(
                    ModParticles.NIGHT_RIPPLE,
                    position.x + (level.random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 1.01,
                    position.z + (level.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0
            );
        } else {
            level.addParticle(
                    ModParticles.ERROR_RIPPLE,
                    position.x + (level.random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 1.01,
                    position.z + (level.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0
            );
        }
    }
}
