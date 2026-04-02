package com.yelf42.paradise.items;

import com.yelf42.paradise.registry.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// TODO add different visuals for empty vs written discs (post 1.21.4)
public class AccessDiscItem extends Item {
    public AccessDiscItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ModComponents.DimensionAddressComponent address = stack.get(ModComponents.DIMENSION_ADDRESS);
        if (address != null) {
            address.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }
}
