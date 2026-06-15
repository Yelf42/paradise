package com.yelf42.paradise.items;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DataReaderBlockEntity;
import com.yelf42.paradise.blocks.DataServerBlockEntity;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModComponents;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public class BackupRecordItem extends Item {

    public static final Predicate<ItemStack> VALID_BACKUP = stack -> {
        if (!stack.is(ModItems.BACKUP_RECORD)) return false;
        if (!stack.has(ModComponents.DIMENSION_ADDRESS)) return false;

        String path = stack.getOrDefault(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(Paradise.identifier(""))).address().getPath();

        return !(path.isEmpty() || path.equals("nullspace"));
    };

    public BackupRecordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack inHand = context.getItemInHand();

        if (state.is(ModBlocks.DATA_CORE)) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        if (state.is(ModBlocks.DATA_SERVER)) {
            if (level instanceof ServerLevel serverLevel) {
                DataServerBlockEntity dataServerBlockEntity = serverLevel.getBlockEntity(pos, ModBlockEntities.DATA_SERVER).orElse(null);
                if (dataServerBlockEntity == null) return super.useOn(context);
                inHand.set(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(dataServerBlockEntity.getDimension()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        } else if (state.is(ModBlocks.DATA_READER)) {
            if (level instanceof ServerLevel serverLevel) {
                DataReaderBlockEntity dataReaderBlockEntity = serverLevel.getBlockEntity(pos, ModBlockEntities.DATA_READER).orElse(null);
                if (dataReaderBlockEntity == null) return super.useOn(context);
                inHand.set(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(dataReaderBlockEntity.getDimension()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ModComponents.DimensionAddressComponent address = stack.get(ModComponents.DIMENSION_ADDRESS);
        if (address != null) {
            address.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return VALID_BACKUP.test(stack) || super.isFoil(stack);
    }
}
