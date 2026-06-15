package com.yelf42.paradise.items;

import com.yelf42.paradise.registry.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AddressChipItem extends Item {
    public AddressChipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ModComponents.DownloaderAddressComponent address = stack.get(ModComponents.DOWNLOADER_ADDRESS);
        if (address != null) {
            address.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }
}
