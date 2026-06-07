package com.yelf42.paradise.client.renderer;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.Map;

public class ModClientModels {
    public static final Map<ItemLike, ModelResourceLocation> CUSTOM_GUI_MODELS = Map.of(
            ModItems.GARDENING_STAFF, ModelResourceLocation.inventory(Paradise.identifier("gardening_staff_gui")),
            ModItems.SCULPTING_STAFF, ModelResourceLocation.inventory(Paradise.identifier("sculpting_staff_gui"))
    );
}
