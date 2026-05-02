package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.items.AccessDiscItem;
import com.yelf42.paradise.items.ServerLocatorItem;
import com.yelf42.paradise.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModItems {

    // ITEMS
    public static final LinkedHashMap<String, Item> REGISTERED_ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Item> REGISTERED_CREATIVE_ITEMS = new LinkedHashMap<>();

    public static final Item ACCESS_DISC = registerItem("access_disc", AccessDiscItem::new, new Item.Properties().rarity(Rarity.RARE).stacksTo(1).component(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(Paradise.identifier(""))));
    public static final Item SERVER_LOCATOR = registerItem("server_locator", ServerLocatorItem::new, new Item.Properties().rarity(Rarity.RARE).stacksTo(1).component(ModComponents.SERVER_LOCATION, new ModComponents.ServerLocatorComponent(null)));


    private static ResourceKey<Item> vanillaItemId(String name) {
        return ResourceKey.create(Registries.ITEM, Paradise.identifier(name));
    }

    public static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        return registerItem(vanillaItemId(name), factory, properties);
    }

    public static Item registerSpawnEgg(String name, EntityType<? extends Mob> entityType) {
        return registerItem(vanillaItemId(name), (properties -> new SpawnEggItem(entityType, 0xFFFFFF, 0xFFFFFF, properties)));
    }

    public static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> factory) {
        return registerItem(key, factory, new Item.Properties());
    }

    public static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> factory, Item.Properties properties) {
        var item = factory.apply(properties);
        REGISTERED_ITEMS.put(key.location().getPath(), item);

        return item;
    }

    /// BINDER
    public static void registerItems(BiConsumer<Item, ResourceLocation> consumer) {
        REGISTERED_ITEMS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
        REGISTERED_CREATIVE_ITEMS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }


    // TAB
    public static final CreativeModeTab PARADISE_TAB = Services.PLATFORM.tabBuilder()
            .icon(() -> new ItemStack(ModBlocks.DIGITAL_GRASS_BARRIER.asItem()))
            .title(Component.translatable("itemGroup.paradise"))
            .displayItems((itemDisplayParameters, output) -> {
                ModItems.REGISTERED_ITEMS.forEach((s, item) -> output.accept(item));
                ModBlocks.REGISTERED_BLOCK_ITEMS.forEach((s, item) -> output.accept(item));
            }).build();

    public static final CreativeModeTab PARADISE_CREATIVE_TAB = Services.PLATFORM.tabBuilder()
            .icon(() -> new ItemStack(ModBlocks.DIGITAL_VOLUME_BARRIER.asItem()))
            .title(Component.translatable("itemGroup.paradise_creative"))
            .displayItems((itemDisplayParameters, output) -> {
                ModItems.REGISTERED_CREATIVE_ITEMS.forEach((s, item) -> output.accept(item));
                ModBlocks.REGISTERED_CREATIVE_BLOCK_ITEMS.forEach((s, item) -> output.accept(item));
            }).build();

    /// BINDER
    public static void registerTabs(BiConsumer<CreativeModeTab, ResourceLocation> consumer) {
        consumer.accept(PARADISE_TAB, ResourceLocation.fromNamespaceAndPath(Paradise.MOD_ID, "paradise_tab"));
        consumer.accept(PARADISE_CREATIVE_TAB, ResourceLocation.fromNamespaceAndPath(Paradise.MOD_ID, "paradise_creative_tab"));
    }


    // RECIPES

    /// BINDER
    public static void registerRecipes(BiConsumer<RecipeSerializer<?>, ResourceLocation> consumer) {
    }
}
