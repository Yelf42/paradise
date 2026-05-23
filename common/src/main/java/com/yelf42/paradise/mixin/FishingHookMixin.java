package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Inject(method = "pullEntity", at = @At("HEAD"))
    protected void digitalFishing(Entity entity, CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;
        Player player = self.getPlayerOwner();

        // TODO loot table
        if (player != null && entity instanceof DigitalFish digitalFish) {
            ItemStack tool = player.getMainHandItem();
            if (!tool.is(Items.FISHING_ROD)) tool = new ItemStack(Items.FISHING_ROD);

            LootParams lootparams = (new LootParams.Builder((ServerLevel)self.level())).withParameter(LootContextParams.ORIGIN, self.position()).withParameter(LootContextParams.THIS_ENTITY, self).withParameter(LootContextParams.TOOL, tool).withLuck(player.getLuck()).create(LootContextParamSets.FISHING);
            LootTable loottable = self.level().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Paradise.identifier("gameplay/digital_fishing")));
            //LootTable loottable = self.level().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("gameplay/digital_fishing")));

            digitalFish.setAttacker(player.getUUID());

            List<ItemStack> list = loottable.getRandomItems(lootparams);

            for (ItemStack stack : list) {
                ItemEntity itementity = new ItemEntity(self.level(), self.getX(), self.getY(), self.getZ(), stack);
                double d0 = player.getX() - self.getX();
                double d1 = player.getY() - self.getY();
                double d2 = player.getZ() - self.getZ();
                itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.1);
                self.level().addFreshEntity(itementity);
            }
        }
    }

}
