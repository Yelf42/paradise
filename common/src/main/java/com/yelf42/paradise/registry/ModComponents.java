package com.yelf42.paradise.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yelf42.paradise.Paradise;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModComponents {

    // Item components
    public static final DataComponentType<DimensionAddressComponent> DIMENSION_ADDRESS = DataComponentType.<DimensionAddressComponent>builder().persistent(DimensionAddressComponent.CODEC).build();
    public record DimensionAddressComponent(ResourceLocation address) implements TooltipProvider {
        public static final Codec<DimensionAddressComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    ResourceLocation.CODEC.fieldOf("address").forGetter(DimensionAddressComponent::address)
            ).apply(builder, DimensionAddressComponent::new);
        });

        // Address should only contain numbers, lowercase letters, underscores

        @Override
        public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
            String print = (address.getPath().isEmpty()) ? "Empty" : address.getPath().toUpperCase();
            tooltip.accept(Component.literal(print).withStyle(ChatFormatting.GRAY));
        }
    }

    public static final DataComponentType<ServerLocatorComponent> SERVER_LOCATION = DataComponentType.<ServerLocatorComponent>builder().persistent(ServerLocatorComponent.CODEC).build();
    public record ServerLocatorComponent(BlockPos location) implements TooltipProvider {
        public static final Codec<ServerLocatorComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    BlockPos.CODEC.fieldOf("location").forGetter(ServerLocatorComponent::location)
            ).apply(builder, ServerLocatorComponent::new);
        });
        @Override
        public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
            String print = (location == null) ? "IDLE" : "ACTIVE";
            tooltip.accept(Component.literal(print).withStyle(ChatFormatting.GRAY));
        }
    }

    /// BINDER
    public static void register(BiConsumer<DataComponentType<?>, ResourceLocation> consumer) {
        consumer.accept(DIMENSION_ADDRESS, Paradise.identifier("dimension_address"));
        consumer.accept(SERVER_LOCATION, Paradise.identifier("server_location"));
    }
}
