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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParadiseDimensionSavedData extends SavedData {
    private static final String ID = "paradise_dimensions";
    private final List<ResourceLocation> dimensions = new ArrayList<>();

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
        ListTag list = tag.getList("dimensions", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.dimensions.add(ResourceLocation.parse(list.getString(i)));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (ResourceLocation id : dimensions) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put("dimensions", list);
        return tag;
    }

    public void addDimension(ResourceLocation id) {
        if (!dimensions.contains(id)) {
            dimensions.add(id);
            setDirty();
        }
    }

    public List<ResourceLocation> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }

}