package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DigitalVolumeBlock extends Block {

    public static final MapCodec<DigitalVolumeBlock> CODEC = simpleCodec(DigitalVolumeBlock::new);

    public DigitalVolumeBlock(Properties properties) {
        super(properties);
    }

    public MapCodec<DigitalVolumeBlock> codec() {
        return CODEC;
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean digital = context.getLevel().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital) return null;
        return super.getStateForPlacement(context);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) {
            if (neighborsAir(level, pos) && neighborsDigitalGrass(level, pos)) {
                boolean barrier = state.is(ModBlocks.DIGITAL_VOLUME_BARRIER);
                level.setBlock(pos, barrier ? ModBlocks.DIGITAL_GRASS_BARRIER.defaultBlockState() : ModBlocks.DIGITAL_GRASS.defaultBlockState(), 3);
            }
        }
    }

    private boolean neighborsAir(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.above()).isAir();
    }

    private boolean neighborsDigitalGrass(ServerLevel level, BlockPos pos) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    BlockState toCheck = level.getBlockState(pos.offset(i,j,k));
                    if (toCheck.is(ModBlocks.DIGITAL_GRASS) || toCheck.is(ModBlocks.DIGITAL_GRASS_BARRIER)) return true;
                }
            }
        }
        return false;
    }


}
