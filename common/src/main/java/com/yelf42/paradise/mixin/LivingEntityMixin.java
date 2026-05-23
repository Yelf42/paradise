package com.yelf42.paradise.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.yelf42.paradise.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    private Iterator<MobEffectInstance> paradise$filterEffects(Iterator<MobEffectInstance> original) {
        return new Iterator<>() {
            private MobEffectInstance next;

            @Override
            public boolean hasNext() {
                while (next == null && original.hasNext()) {
                    MobEffectInstance candidate = original.next();

                    if (!paradise$shouldPreserve(candidate)) {
                        next = candidate;
                    }
                }

                return next != null;
            }

            @Override
            public MobEffectInstance next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                MobEffectInstance result = next;
                next = null;
                return result;
            }

            @Override
            public void remove() {
                original.remove();
            }
        };
    }


    @Unique
    private boolean paradise$shouldPreserve(MobEffectInstance effect) {
        return effect != null && effect.is(ModEffects.EJECT);
    }
}
