package com.yelf42.paradise.client.renderer.entity;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.entities.DigitalArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DigitalArrowRenderer extends ArrowRenderer<DigitalArrow> {
    public static final ResourceLocation DIGITAL_ARROW_LOCATION = Paradise.identifier("textures/entity/projectiles/digital_arrow.png");

    public DigitalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(DigitalArrow entity) {
        return DIGITAL_ARROW_LOCATION;
    }
}