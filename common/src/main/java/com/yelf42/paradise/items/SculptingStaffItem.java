package com.yelf42.paradise.items;

import com.yelf42.paradise.blocks.DigitalSculpture;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SculptingStaffItem extends Item {

    private static final GardeningStaffItem.CyclicOrderedSet<Block> QUERY_DIGITAL_SCULPTURES = new GardeningStaffItem.CyclicOrderedSet<>(List.of(
            ModBlocks.DIGITAL_SCULPTURE_1,
            ModBlocks.DIGITAL_SCULPTURE_2,
            ModBlocks.DIGITAL_SCULPTURE_3,
            ModBlocks.DIGITAL_SCULPTURE_5,
            ModBlocks.DIGITAL_SCULPTURE_4
    ));

    public SculptingStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return state.getBlock() instanceof DigitalSculpture;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean clientSide = level.isClientSide();
        BlockState state = level.getBlockState(pos);

        Direction face = context.getClickedFace();
        if (QUERY_DIGITAL_SCULPTURES.contains(state.getBlock()) && !(face.equals(Direction.UP) || face.equals(Direction.DOWN))) {
            Block newBlock = QUERY_DIGITAL_SCULPTURES.next(state.getBlock());
            if (newBlock == null) return InteractionResult.PASS;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlock(pos, newBlock.withPropertiesOf(state), 2);
            }
            return InteractionResult.sidedSuccess(clientSide);
        }

        InteractionResult placement = ModBlocks.DIGITAL_SCULPTURE_1.asItem().useOn(context);
        if (placement.indicateItemUse()) context.getItemInHand().grow(1);
        return placement;
    }
}
