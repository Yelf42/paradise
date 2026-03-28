package com.yelf42.paradise;

import com.yelf42.paradise.platform.Services;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Paradise {

    public static final String MOD_ID = "paradise";
    public static final String MOD_NAME = "Paradise";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"}; // Nice for components

    public static void init() {
        LOG.info("Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(Paradise.MOD_ID, path);
    }
}