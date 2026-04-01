package com.yelf42.paradise.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// TODO humming sound
public class DataCoreBlock extends BaseEntityBlock {

    public static final MapCodec<DataCoreBlock> CODEC = simpleCodec(DataCoreBlock::new);

    public DataCoreBlock(Properties properties) {
        super(properties);
    }

    public MapCodec<DataCoreBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DataCoreBlockEntity(blockPos, blockState);
    }
}
