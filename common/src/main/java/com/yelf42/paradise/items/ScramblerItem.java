package com.yelf42.paradise.items;

import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.registry.ModEffects;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// TODO assets
public class ScramblerItem extends Item {
    public ScramblerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            if (player.hasEffect(ModEffects.EJECT)) return;
            if (player.getMainHandItem().is(ModItems.SCRAMBLER) || player.getOffhandItem().is(ModItems.SCRAMBLER)) return;
            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(serverLevel);
            if (!intrudersSavedData.isIntruder(player.getUUID())) return;

            player.addEffect(ModEffects.ejectInstance());
        }
    }
}
