package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

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

}
