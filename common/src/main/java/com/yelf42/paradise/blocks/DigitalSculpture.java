package com.yelf42.paradise.blocks;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

// TODO test shading in future versions
public class DigitalSculpture extends Block {

    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 7);

    private static final VoxelShape SHAPE_0 = Block.box(1, 0, 5, 15, 16, 11);
    private static final VoxelShape SHAPE_1 = Shapes.or(
            Block.box(0, 0, 3, 8, 16, 9),
            Block.box(4, 0, 5, 12, 16, 11),
            Block.box(8, 0, 7, 16, 16, 13)
    );
    private static final VoxelShape SHAPE_2 = Shapes.or(
            Block.box(1, 0, 1, 7, 16, 7),
            Block.box(3, 0, 3, 9, 16, 9),
            Block.box(5, 0, 5, 11, 16, 11),
            Block.box(7, 0, 7, 13, 16, 13),
            Block.box(9, 0, 9, 15, 16, 15)
    );
    private static final VoxelShape SHAPE_3 = Shapes.or(
            Block.box(3, 0, 0, 9, 16, 8),
            Block.box(5, 0, 4, 11, 16, 12),
            Block.box(7, 0, 8, 13, 16, 16)
    );
    private static final VoxelShape SHAPE_4 = Block.box(5, 0, 1, 11, 16, 15);
    private static final VoxelShape SHAPE_5 = Shapes.or(
            Block.box(7, 0, 0, 13, 16, 8),
            Block.box(5, 0, 4, 11, 16, 12),
            Block.box(3, 0, 8, 9, 16, 16)
    );
    private static final VoxelShape SHAPE_6 = Shapes.or(
            Block.box(9, 0, 1, 15, 16, 7),
            Block.box(7, 0, 3, 13, 16, 9),
            Block.box(5, 0, 5, 11, 16, 11),
            Block.box(3, 0, 7, 9, 16, 13),
            Block.box(1, 0, 9, 7, 16, 15)
    );
    private static final VoxelShape SHAPE_7 = Shapes.or(
            Block.box(8, 0, 3, 16, 16, 9),
            Block.box(4, 0, 5, 12, 16, 11),
            Block.box(0, 0, 7, 8, 16, 13)
    );
    private static final VoxelShape[] SHAPES = {
            SHAPE_0, SHAPE_1, SHAPE_2, SHAPE_3,
            SHAPE_4, SHAPE_5, SHAPE_6, SHAPE_7
    };

    public DigitalSculpture(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState defaultState = super.getStateForPlacement(context);
        Level level = context.getLevel();
        boolean digital = level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS);
        if (!digital || defaultState == null) return null;

        float rot = context.getRotation();
        float normalized = ((rot % 360) + 360) % 360;
        //int rotation = 2 * (Math.round(normalized / 45f) % 8);
        int rotation = (Math.round(normalized / 22.5f) % 8);
        return defaultState.setValue(ROTATION, rotation);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(ROTATION)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }
}
