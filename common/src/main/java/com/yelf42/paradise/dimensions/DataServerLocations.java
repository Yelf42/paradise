package com.yelf42.paradise.dimensions;

import com.yelf42.paradise.Paradise;
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

import java.util.HashMap;
import java.util.Map;

public class DataServerLocations extends SavedData {
    private static final String ID = "data_server_locations";
    private final Map<ResourceLocation, Pair<BlockPos, ResourceLocation>> servers = new HashMap<>();

    public static DataServerLocations getOrCreate(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DataServerLocations::new,
                        DataServerLocations::load,
                        DataFixTypes.LEVEL
                ),
                ID
        );
    }

    private static DataServerLocations load(CompoundTag tag, HolderLookup.Provider provider) {
        DataServerLocations data = new DataServerLocations();
        ListTag list = tag.getList("servers", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation id = ResourceLocation.parse(entry.getString("id"));
            BlockPos pos = NbtUtils.readBlockPos(entry, "pos").orElseThrow();
            ResourceLocation dim = ResourceLocation.parse(entry.getString("dim"));
            data.servers.put(id, Pair.of(pos, dim));
            //Paradise.LOGGER.info("DataServerLocation: {} {} {}", id, pos, dim);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceLocation, Pair<BlockPos, ResourceLocation>> entry : servers.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.putString("id", entry.getKey().toString());
            compound.put("pos", NbtUtils.writeBlockPos(entry.getValue().getLeft()));
            compound.putString("dim", entry.getValue().getRight().toString());
            list.add(compound);
        }
        tag.put("servers", list);
        return tag;
    }

    public boolean add(ResourceLocation dimId, BlockPos pos, ResourceLocation hostDimId) {
        if (servers.containsKey(dimId)) return false;
        servers.put(dimId, Pair.of(pos,hostDimId));
        setDirty();
        return true;
    }

    public boolean remove(ResourceLocation dimId, BlockPos pos, ResourceLocation hostDimId) {
        if (!servers.containsKey(dimId)) return false;
        servers.remove(dimId);
        setDirty();
        return true;
    }

    public @Nullable Pair<BlockPos, ResourceLocation> get(ResourceLocation dim) {
        return servers.getOrDefault(dim, null);
    }
}
