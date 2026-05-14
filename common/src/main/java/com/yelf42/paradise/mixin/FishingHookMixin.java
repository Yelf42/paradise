package com.yelf42.paradise.mixin;

import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Inject(method = "pullEntity", at = @At("HEAD"))
    protected void digitalFishing(Entity entity, CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;
        Player player = self.getPlayerOwner();

        // TODO loot table
        if (player != null && entity instanceof DigitalFish digitalFish) {
            digitalFish.setAttacker(player.getUUID());

            ItemStack stack = new ItemStack(ModBlocks.DIGITAL_VOLUME);

            ItemEntity itementity = new ItemEntity(self.level(), self.getX(), self.getY(), self.getZ(), stack);
            double d0 = player.getX() - self.getX();
            double d1 = player.getY() - self.getY();
            double d2 = player.getZ() - self.getZ();
            itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.1);
            self.level().addFreshEntity(itementity);
        }
    }

}
