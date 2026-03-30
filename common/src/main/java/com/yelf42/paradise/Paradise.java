package com.yelf42.paradise;

import com.yelf42.paradise.platform.Services;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO world border in dimensions
// TODO digital grass
// TODO digital sky block
// TODO digital skybox
public class Paradise {

    public static final String MOD_ID = "paradise";
    public static final String MOD_NAME = "Paradise";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"}; // Nice for components

    public static final Vector3f PARADISE_SKY = new Vector3f(60/ 255.0f, 119/ 255.0f, 239/ 255.0f);
    public static final Vector3f PARADISE_SKY_REFLECTION = new Vector3f(30/ 255.0f, 100/ 255.0f, 239/ 255.0f);

    public static void init() {
        LOGGER.info("Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(Paradise.MOD_ID, path);
    }
}