package com.yelf42.paradise.dimensions;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParadiseDimensionSavedData extends SavedData {
    private static final String ID = "paradise_dimensions";
    private final Map<ResourceLocation, DimensionRegistry.ParadiseType> dimensions = new ConcurrentHashMap<>();

    public static ParadiseDimensionSavedData getOrCreate(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        ParadiseDimensionSavedData::new,
                        ParadiseDimensionSavedData::load,
                        DataFixTypes.LEVEL
                ),
                ID
        );
    }

    public static ParadiseDimensionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ParadiseDimensionSavedData data = new ParadiseDimensionSavedData();
        ListTag list1 = tag.getList("dimensionsIds", Tag.TAG_STRING);
        ListTag list2 = tag.getList("dimensionsTypes", Tag.TAG_STRING);
        for (int i = 0; i < list1.size(); i++) {
            data.dimensions.put(ResourceLocation.parse(list1.getString(i)), DimensionRegistry.ParadiseType.valueOf(list2.getString(i)));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list1 = new ListTag();
        ListTag list2 = new ListTag();
        for (Map.Entry<ResourceLocation, DimensionRegistry.ParadiseType> pair : dimensions.entrySet()) {
            list1.add(StringTag.valueOf(pair.getKey().toString()));
            list2.add(StringTag.valueOf(pair.getValue().toString()));
        }
        tag.put("dimensionsIds", list1);
        tag.put("dimensionsTypes", list2);
        return tag;
    }

    public boolean containsDimension(ResourceLocation id) {
        return dimensions.containsKey(id);
    }

    public void addDimension(ResourceLocation id, DimensionRegistry.ParadiseType type) {
        if (!dimensions.containsKey(id)) {
            dimensions.put(id, type);
            setDirty();
        }
    }

    public void deleteDimension(ResourceLocation id) {
        if (dimensions.containsKey(id)) {
            dimensions.remove(id);
            setDirty();
        }
    }

    public Map<ResourceLocation, DimensionRegistry.ParadiseType> getDimensions() {
        return Collections.unmodifiableMap(dimensions);
    }

}