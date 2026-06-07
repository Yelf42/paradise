package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.items.BackupRecordItem;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Final
    @Shadow
    public Player player;

    /**
     * More BackupItem code found at {@link com.yelf42.paradise.mixin.ServerPlayerMixin}
     */
    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    public void dropAll(CallbackInfo ci) {
        Inventory inventory = (Inventory) (Object) this;
        if (inventory.contains(BackupRecordItem.VALID_BACKUP)) {
            ci.cancel();

            int toSave = Paradise.CONFIG.backupSaves;

            List<Integer> indexShuffle = IntStream.range(0, inventory.items.size()).boxed().collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(indexShuffle);

            for (int i : indexShuffle) {
                ItemStack stack = inventory.items.get(i);
                boolean inHotbar = Inventory.isHotbarSlot(i);
                if (!stack.isEmpty() && !inHotbar && !stack.is(ModItems.BACKUP_RECORD)) {
                    if (toSave > 0) {
                        toSave--;
                    } else {
                        this.player.drop(stack, true, false);
                        inventory.items.set(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
