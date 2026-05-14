package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    // TODO test for player
    @Inject(method = "shouldShowName*", at = @At("HEAD"), cancellable = true)
    protected void shouldShowName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!entity.level().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) return;

        ItemStack offhand = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainhand.is(Items.STICK) || offhand.is(Items.STICK)) {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }

}
