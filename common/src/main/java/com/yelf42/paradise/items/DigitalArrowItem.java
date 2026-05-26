package com.yelf42.paradise.items;

import com.yelf42.paradise.entities.DigitalArrow;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DigitalArrowItem extends ArrowItem {
    public DigitalArrowItem(Item.Properties properties) {
        super(properties);
    }

    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity livingEntity, ItemStack itemStack) {
        return new DigitalArrow(level, livingEntity, stack.copyWithCount(1), itemStack);
    }

    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        DigitalArrow digitalArrow = new DigitalArrow(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
        digitalArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        return digitalArrow;
    }
}