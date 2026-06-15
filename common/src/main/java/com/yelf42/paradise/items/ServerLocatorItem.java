package com.yelf42.paradise.items;

import com.mojang.datafixers.util.Pair;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModComponents;
import com.yelf42.paradise.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public class ServerLocatorItem extends Item {

    public ServerLocatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!Inventory.isHotbarSlot(slotId)) return;

        if (entity instanceof Player player) {
            BlockPos entityPos = entity.blockPosition();

            ModComponents.ServerLocatorComponent serverLocatorComponent = stack.get(ModComponents.SERVER_LOCATION);
            if (serverLocatorComponent == null) return;

            BlockPos storedPos = serverLocatorComponent.location();
            if (storedPos == null) {
                if (level instanceof ServerLevel serverLevel) {
                    if (level.getGameTime() % 400 == 0) {
                        BlockPos bunkerPos = locateBunker(serverLevel, entityPos);
                        if (bunkerPos != null) {
                            long dx = bunkerPos.getX() - entityPos.getX();
                            long dz = bunkerPos.getZ() - entityPos.getZ();
                            long distSqrXZ = dx * dx + dz * dz;
                            if (distSqrXZ <= 60000) {
                                stack.set(ModComponents.SERVER_LOCATION, new ModComponents.ServerLocatorComponent(bunkerPos));
                            }
                        }
                    }
                }
                return;
            }

            if (level instanceof ServerLevel) {
                return;
            }

            int dist = (entityPos.getX() - storedPos.getX()) * (entityPos.getX() - storedPos.getX()) + (entityPos.getZ() - storedPos.getZ()) * (entityPos.getZ() - storedPos.getZ());
            dist = (int)Math.sqrt(dist);

            if (dist > 256) {
                stack.set(ModComponents.SERVER_LOCATION, new ModComponents.ServerLocatorComponent(null));
                return;
            }

            int interval = Math.max((dist / 16) * 4, 4);

            long lastSound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getLong("lastSound");

            long gameTime = level.getGameTime();
            if (gameTime - lastSound >= interval) {
                level.playSound(player, entityPos, ModSounds.SERVER_LOCATOR_PING, SoundSource.PLAYERS, 0.25f, 1.0f + 0.1f * (level.getRandom().nextFloat()));

                CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                tag.putLong("lastSound", gameTime);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }

    }

    private static BlockPos locateBunker(ServerLevel serverLevel, BlockPos pos) {
        Registry<Structure> registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure structure = registry.get(Paradise.identifier("bunker"));
        if (structure == null) return null;

        HolderSet<Structure> holderset = HolderSet.direct(Holder.direct(structure));
        Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderset, pos, 20, false);
        if (pair == null) return null;
        return pair.getFirst();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ModComponents.ServerLocatorComponent serverLocatorComponent = stack.get(ModComponents.SERVER_LOCATION);
        if (serverLocatorComponent != null) {
            serverLocatorComponent.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }
}
