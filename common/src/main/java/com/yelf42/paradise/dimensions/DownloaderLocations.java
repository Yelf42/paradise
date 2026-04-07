package com.yelf42.paradise.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloaderLocations extends SavedData {
    private static final String ID = "downloader_locations";

    // <Downloader ID, <Position, Dimension>>
    private final Map<String, Pair<BlockPos, ResourceLocation>> downloaders = new ConcurrentHashMap<>();

    public static DownloaderLocations getOrCreate(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DownloaderLocations::new,
                        DownloaderLocations::load,
                        DataFixTypes.LEVEL
                ),
                ID
        );
    }

    private static DownloaderLocations load(CompoundTag tag, HolderLookup.Provider provider) {
        DownloaderLocations data = new DownloaderLocations();
        ListTag list = tag.getList("servers", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(entry, "pos").orElseThrow();
            ResourceLocation dim = ResourceLocation.parse(entry.getString("dim"));
            data.downloaders.put(entry.getString("id"), Pair.of(pos, dim));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<String, Pair<BlockPos, ResourceLocation>> entry : downloaders.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.putString("id", entry.getKey());
            compound.put("pos", NbtUtils.writeBlockPos(entry.getValue().getLeft()));
            compound.putString("dim", entry.getValue().getRight().toString());
            list.add(compound);
        }
        tag.put("servers", list);
        return tag;
    }

    public boolean add(String dimId, BlockPos pos, ResourceLocation hostDimId) {
        if (downloaders.containsKey(dimId)) return false;
        downloaders.put(dimId, Pair.of(pos,hostDimId));
        setDirty();
        return true;
    }

    public void remove(String dimId) {
        downloaders.remove(dimId);
        setDirty();
    }

    public @Nullable Pair<BlockPos, ResourceLocation> get(String id) {
        return downloaders.getOrDefault(id, null);
    }

    public boolean has(String id) {
        return downloaders.containsKey(id);
    }
}
