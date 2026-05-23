package com.yelf42.paradise.effects;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;

public class EjectMobEffect extends MobEffect {

    public static final ResourceLocation EJECT_SLOWNESS = Paradise.identifier("effect.eject_slowness");
    public static final int MAX_DURATION = 300;

    public EjectMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        super.onEffectStarted(livingEntity, amplifier);
        // TODO sfx that lasts until ejection
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        MobEffectInstance instance = entity.getEffect(ModEffects.EJECT);
        if (instance == null) return true;
        int duration = instance.getDuration();
        float t = (float) duration / MAX_DURATION;

        AttributeInstance speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.addOrUpdateTransientModifier(new AttributeModifier(EJECT_SLOWNESS, -1.0 + t * t, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (duration <= 10) {
            if (speedAttr != null) {
                speedAttr.removeModifier(EJECT_SLOWNESS);
            }

            entity.removeEffect(ModEffects.EJECT);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.changeDimension(serverPlayer.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING));
            }
        }

        return true;
    }

    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

}
