package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModItems {

    // ITEMS
    public static final LinkedHashMap<String, Item> REGISTERED_ITEMS = new LinkedHashMap<>();

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
    }


    // TAB
    public static final CreativeModeTab PARADISE_TAB = Services.PLATFORM.tabBuilder()
            .icon(() -> new ItemStack(ModBlocks.DIGITAL_GRASS_BARRIER.asItem()))
            .title(Component.translatable("itemGroup.paradise"))
            .displayItems((itemDisplayParameters, output) -> {
                ModItems.REGISTERED_ITEMS.forEach((s, item) -> output.accept(item));
                ModBlocks.REGISTERED_BLOCK_ITEMS.forEach((s, item) -> output.accept(item));
            }).build();

    /// BINDER
    public static void registerTabs(BiConsumer<CreativeModeTab, ResourceLocation> consumer) {
        consumer.accept(PARADISE_TAB, ResourceLocation.fromNamespaceAndPath(Paradise.MOD_ID, "paradise_tab"));
    }


    // RECIPES

    /// BINDER
    public static void registerRecipes(BiConsumer<RecipeSerializer<?>, ResourceLocation> consumer) {
    }
}
