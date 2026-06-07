package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.dimensions.TransitLogSavedData;
import com.yelf42.paradise.dimensions.WhitelistsSavedData;
import com.yelf42.paradise.items.BackupRecordItem;
import com.yelf42.paradise.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Final
    @Shadow
    public MinecraftServer server;

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at  = @At("HEAD"), cancellable = true)
    public void digitalWorldRespawn(boolean keepInventory, DimensionTransition.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = (ServerLevel) player.level();

        if (Paradise.CONFIG.safeRespawn && level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) {

            boolean inNullspace = level.dimensionTypeRegistration().is(
                    ResourceKey.create(Registries.DIMENSION_TYPE,
                            Paradise.identifier("paradise_dimension_error")));

            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(level);
            boolean isIntruder = intrudersSavedData.isIntruder(player.getUUID());

            if (!inNullspace && !isIntruder) {
                BlockPos spawnPos = new BlockPos(56, 6, 0);
                cir.setReturnValue(new DimensionTransition(
                        level,
                        Vec3.atBottomCenterOf(spawnPos),
                        Vec3.ZERO,
                        Direction.WEST.toYRot(),
                        player.getXRot(),
                        postDimensionTransition
                ));
            }
        }

        ItemStack backup = findInInventory(player.getInventory(), BackupRecordItem.VALID_BACKUP);
        if (!backup.isEmpty()) {
            ResourceLocation dimId = backup.get(ModComponents.DIMENSION_ADDRESS).address();
            ServerLevel backupLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimId));
            if (backupLevel == null) return;

            WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());
            if (whitelistsSavedData.isWhitelisted(dimId, player.getName().getString())) {
                BlockPos spawnPos = new BlockPos(56, 6, 0);
                cir.setReturnValue(new DimensionTransition(
                        backupLevel,
                        Vec3.atBottomCenterOf(spawnPos),
                        Vec3.ZERO,
                        Direction.WEST.toYRot(),
                        player.getXRot(),
                        postDimensionTransition
                ));
            } else {
                TransitLogSavedData transitLogSavedData = TransitLogSavedData.getOrCreate(backupLevel);
                transitLogSavedData.addLog(TransitLogSavedData.createLogEntry(false, "BACKUP", "DENIED ENTRY"));
                player.displayClientMessage(Component.translatable("gui.paradise.teleport.not_whitelisted").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    /**
     * More BackupItem code found at {@link com.yelf42.paradise.mixin.InventoryMixin}
     */
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    public void backupItems(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (keepEverything) return;
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || that.isSpectator()) return;

        if (that.getInventory().contains(BackupRecordItem.VALID_BACKUP)) {
            player.getInventory().replaceWith(that.getInventory());
        }
    }

    private ItemStack findInInventory(Inventory inventory, Predicate<ItemStack> predicate) {
        for (List<ItemStack> compartment : List.of(inventory.items, inventory.armor, inventory.offhand)) {
            for (ItemStack stack : compartment) {
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
