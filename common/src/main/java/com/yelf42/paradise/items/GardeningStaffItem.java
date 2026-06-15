package com.yelf42.paradise.items;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DigitalSculpture;
import com.yelf42.paradise.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GardeningStaffItem extends Item {

    public static class CyclicOrderedSet<T> {
        private final List<T> list;
        private final java.util.Map<T, Integer> indexMap;

        public CyclicOrderedSet(List<T> items) {
            this.list = List.copyOf(items);
            this.indexMap = new java.util.HashMap<>();
            for (int i = 0; i < items.size(); i++) {
                indexMap.put(items.get(i), i);
            }
        }

        public T next(T current) {
            if (list.isEmpty()) return null;
            Integer idx = indexMap.get(current);
            if (idx == null) return null;
            return list.get((idx + 1) % list.size());
        }

        public boolean contains(T item) {
            return indexMap.containsKey(item);
        }
    }

    private static final CyclicOrderedSet<String> QUERY_COLORS = new CyclicOrderedSet<>(List.of(
            "white",
            "light_gray",
            "gray",
            "black",
            "brown",
            "red",
            "orange",
            "yellow",
            "lime",
            "green",
            "cyan",
            "light_blue",
            "blue",
            "purple",
            "magenta",
            "pink"
    ));

    private static final CyclicOrderedSet<Block> QUERY_DIGITAL_BARRIERS = new CyclicOrderedSet<>(List.of(
            ModBlocks.DIGITAL_BARRIER,
            ModBlocks.DIGITAL_VOLUME_BARRIER,
            ModBlocks.DIGITAL_GRASS_BARRIER,
            ModBlocks.DIGITAL_PILLAR_BARRIER
    ));

    private static final CyclicOrderedSet<DyeColor> QUERY_DYE_COLORS = new CyclicOrderedSet<>(List.of(
            DyeColor.WHITE,
            DyeColor.LIGHT_GRAY,
            DyeColor.GRAY,
            DyeColor.BLACK,
            DyeColor.BROWN,
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK
    ));

    public GardeningStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        boolean clientSide = level.isClientSide();

        if (!level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        String blockNamespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();
        String blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        String[] parts = blockName.split("_");
        String result = parts[0].equals("light") ? parts[0] + "_" + parts[1] : parts[0];
        blockName = blockName.replaceFirst(result + "_", "");

        // Rotate colors
        if (QUERY_COLORS.contains(result)) {
            String startColor = result;
            do {
                result = QUERY_COLORS.next(result);

                ResourceLocation newColorIdentifier = ResourceLocation.fromNamespaceAndPath(blockNamespace, result + "_" + blockName);
                if (BuiltInRegistries.BLOCK.containsKey(newColorIdentifier)) {
                    Block newBlock = BuiltInRegistries.BLOCK.get(newColorIdentifier);
                    if (level instanceof ServerLevel serverLevel) {
                        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                        CompoundTag compoundTag = (blockEntity == null) ? null : blockEntity.saveWithoutMetadata(serverLevel.registryAccess());

                        // TODO test
                        if (blockEntity instanceof BaseContainerBlockEntity container) {
                            for (ServerPlayer p : serverLevel.players()) {
                                if (!p.containerMenu.slots.isEmpty() && p.containerMenu.slots.getFirst().container == container) {
                                    p.closeContainer();
                                }
                            }
                        }

                        serverLevel.setBlock(pos, newBlock.withPropertiesOf(state), 2);

                        BlockEntity newBlockEntity = serverLevel.getBlockEntity(pos);
                        if (newBlockEntity != null && compoundTag != null) {
                            newBlockEntity.loadCustomOnly(compoundTag, serverLevel.registryAccess());
                        }
                    }
                    return InteractionResult.sidedSuccess(clientSide);
                }
            } while(result != null && !result.equals(startColor));
            return InteractionResult.PASS;

        // Rotate digital barrier types
        } else if (QUERY_DIGITAL_BARRIERS.contains(state.getBlock())) {
            Block newBlock = QUERY_DIGITAL_BARRIERS.next(state.getBlock());
            if (newBlock == null) return InteractionResult.PASS;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlock(pos, newBlock.withPropertiesOf(state), 2);
            }
            return InteractionResult.sidedSuccess(clientSide);

        // Rotate digital sculpture
        } else if (state.getBlock() instanceof DigitalSculpture) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlock(pos, state.setValue(DigitalSculpture.ROTATION, (state.getValue(DigitalSculpture.ROTATION) + 1) % 8), 2);
            }
            return InteractionResult.sidedSuccess(clientSide);
        }

        return InteractionResult.PASS;
    }

    // Easy to add other entities if their dye methods aren't private
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Sheep sheep) {
            DyeColor currColor = sheep.getColor();
            if (sheep.isAlive() && !sheep.isSheared() && QUERY_DYE_COLORS.contains(currColor) ) {
                if (!player.level().isClientSide) {
                    sheep.setColor(QUERY_DYE_COLORS.next(currColor));
                }

                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}
