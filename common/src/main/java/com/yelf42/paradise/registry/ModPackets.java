package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public class ModPackets {

    public static final ResourceLocation CREATE_DIMENSION_PACKET = Paradise.identifier("create_dimension");
    public static final ResourceLocation REMOVE_DIMENSION_PACKET = Paradise.identifier("remove_dimension");

    public record CreateDimensionPayload(ResourceLocation id, Holder<DimensionType> dimensionType) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<CreateDimensionPayload> ID = new CustomPacketPayload.Type<>(CREATE_DIMENSION_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, CreateDimensionPayload> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, CreateDimensionPayload::id,
                DimensionType.STREAM_CODEC, CreateDimensionPayload::dimensionType,
                CreateDimensionPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RemoveDimensionPayload(ResourceLocation id) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RemoveDimensionPayload> ID = new CustomPacketPayload.Type<>(REMOVE_DIMENSION_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveDimensionPayload> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, RemoveDimensionPayload::id,
                RemoveDimensionPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }


    // Server -> Client: whitelist data response
    public static final ResourceLocation OPEN_WHITELIST_PACKET = Paradise.identifier("open_whitelist");
    public record OpenWhitelistPayload(ResourceLocation dimensionId, Map<String, Long> active, Set<String> history, BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenWhitelistPayload> ID = new CustomPacketPayload.Type<>(OPEN_WHITELIST_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenWhitelistPayload> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, OpenWhitelistPayload::dimensionId,
                ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_LONG), OpenWhitelistPayload::active,
                ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), OpenWhitelistPayload::history,
                BlockPos.STREAM_CODEC, OpenWhitelistPayload::pos,
                OpenWhitelistPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    // Client -> Server: close whitelist screen
    public static final ResourceLocation CLOSE_WHITELIST_PACKET = Paradise.identifier("close_whitelist");
    public record CloseWhitelistPayload(BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<CloseWhitelistPayload> ID = new CustomPacketPayload.Type<>(CLOSE_WHITELIST_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, CloseWhitelistPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, CloseWhitelistPayload::pos,
                CloseWhitelistPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    // Client -> Server: whitelist mutation (add, remove active, remove history)
    public static final ResourceLocation MUTATE_WHITELIST_PACKET = Paradise.identifier("mutate_whitelist");
    public record MutateWhitelistPayload(ResourceLocation dimensionId, String playerName, MutateWhitelistPayload.Action action) implements CustomPacketPayload {
        public enum Action { ADD, REMOVE, FLIP }

        public static final StreamCodec<ByteBuf, Action> ACTION_CODEC = StreamCodec.<ByteBuf, Action>of(
                (buf, action) -> buf.writeInt(action.ordinal()),
                buf -> Action.values()[buf.readInt()]
        );

        public static final CustomPacketPayload.Type<MutateWhitelistPayload> ID = new CustomPacketPayload.Type<>(MUTATE_WHITELIST_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, MutateWhitelistPayload> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, MutateWhitelistPayload::dimensionId,
                ByteBufCodecs.STRING_UTF8, MutateWhitelistPayload::playerName,
                ACTION_CODEC.cast(), MutateWhitelistPayload::action,
                MutateWhitelistPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    // Server -> Client: Open screen to see TransitLog
    public static final ResourceLocation OPEN_TRANSIT_LOG_PACKET = Paradise.identifier("open_transit_log");
    public record OpenTransitLogPayload(ResourceLocation dimensionId, List<String> transitLog) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenTransitLogPayload> ID = new CustomPacketPayload.Type<>(OPEN_TRANSIT_LOG_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenTransitLogPayload> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, OpenTransitLogPayload::dimensionId,
                ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), OpenTransitLogPayload::transitLog,
                OpenTransitLogPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

}
