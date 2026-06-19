package com.yelf42.paradise.client.renderer;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Map;

public class ModClientModels {
    public static final List<ModelResourceLocation> CUSTOM_GUI_MODEL_LOCATIONS = List.of(
            ModelResourceLocation.inventory(Paradise.identifier("gardening_staff_gui")),
            ModelResourceLocation.inventory(Paradise.identifier("sculpting_staff_gui"))
    );

    private static Map<ItemLike, ModelResourceLocation> customGuiModels;

    public static Map<ItemLike, ModelResourceLocation> getCustomGuiModels() {
        if (customGuiModels == null) {
            customGuiModels = Map.of(
                    ModItems.GARDENING_STAFF, ModelResourceLocation.inventory(Paradise.identifier("gardening_staff_gui")),
                    ModItems.SCULPTING_STAFF, ModelResourceLocation.inventory(Paradise.identifier("sculpting_staff_gui"))
            );
        }
        return customGuiModels;
    }
}
