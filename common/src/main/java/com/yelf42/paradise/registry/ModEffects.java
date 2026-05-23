package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.effects.EjectMobEffect;
import com.yelf42.paradise.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ModEffects {

    public static void init() {
        // referencing the class forces static initialization
    }

    public static final Holder<MobEffect> EJECT = register("eject", new EjectMobEffect(MobEffectCategory.HARMFUL, 16711680).addAttributeModifier(Attributes.MOVEMENT_SPEED, EjectMobEffect.EJECT_SLOWNESS, 0.0f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static MobEffectInstance ejectInstance() {
        return new MobEffectInstance(EJECT, EjectMobEffect.MAX_DURATION);
    }

    private static Holder<MobEffect> register(String id, MobEffect statusEffect) {
        return Services.PLATFORM.registerEffectForHolder(Paradise.identifier(id), statusEffect);
    }

}
