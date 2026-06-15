package com.yelf42.paradise.registry;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DataServerBlockEntity;
import com.yelf42.paradise.dimensions.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> DIMENSION_SUGGESTIONS = (context, builder) -> {
        context.getSource().getServer().levelKeys().stream()
                .map(ResourceKey::location)
                .forEach(rl -> {
                    if (rl.getNamespace().equals("paradise")) builder.suggest(rl.toString());
                });
        return builder.buildFuture();
    };

    private static final SimpleCommandExceptionType CANNOT_REMOVE = new SimpleCommandExceptionType(Component.literal("Something went wrong removing dimension").withStyle(ChatFormatting.RED));


    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {

        dispatcher.register(Commands.literal("paradiseTransitLog")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("dimension", ResourceLocationArgument.id()).suggests(DIMENSION_SUGGESTIONS)
                        .executes((context) -> {
                            CommandSourceStack source = context.getSource();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            MinecraftServer server = source.getServer();
                            ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");
                            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));

                            if (level == null) {
                                source.sendFailure(Component.literal("Unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            TransitLogSavedData transitLogSavedData = TransitLogSavedData.getOrCreate(level);
                            source.sendSystemMessage(Component.literal("Transit Log for " + dimensionId + ": "));
                            for (String entry : transitLogSavedData.getTransitLog()) {
                                source.sendSystemMessage(Component.literal(" - " + entry).withStyle(ChatFormatting.YELLOW));
                            }

                            return Command.SINGLE_SUCCESS;
                        })));

        dispatcher.register(Commands.literal("paradiseIntruders")
                        .requires(s -> s.hasPermission(2))
                .then(Commands.argument("dimension", ResourceLocationArgument.id()).suggests(DIMENSION_SUGGESTIONS)
                        .executes((context) -> {
                            CommandSourceStack source = context.getSource();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            MinecraftServer server = source.getServer();
                            ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");
                            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));

                            if (level == null) {
                                source.sendFailure(Component.literal("Unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(level);
                            if (intrudersSavedData.intrudersPresent(level)) {
                                source.sendSystemMessage(Component.literal("There are currently intruders in " + dimensionId + ": "));
                                for (UUID id : intrudersSavedData.getPresentIntruders(level)) {
                                    Entity entity = level.getEntity(id);
                                    if (entity instanceof Player player) {
                                        source.sendSystemMessage(Component.literal(" - " + player.getName()).withStyle(ChatFormatting.YELLOW));
                                    }
                                }
                            } else {
                                source.sendSystemMessage(Component.literal("There are currently no intruders in " + dimensionId));
                                if (intrudersSavedData.totalIntruders() > 0) {
                                    source.sendSystemMessage(Component.literal("These are the UUID's of offline intruders: "));
                                    for (UUID id : intrudersSavedData.getIntruders()) {
                                        source.sendSystemMessage(Component.literal(" - " + id).withStyle(ChatFormatting.YELLOW));
                                    }
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.argument("addPlayer", EntityArgument.player())
                                .executes((context) -> {
                                    CommandSourceStack source = context.getSource();
                                    MinecraftServer server = source.getServer();

                                    if (!source.isPlayer()) {
                                        source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");
                                    ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));

                                    if (level == null) {
                                        source.sendFailure(Component.literal("Unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    ServerPlayer player = EntityArgument.getPlayer(context, "addPlayer");
                                    IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(level);
                                    intrudersSavedData.add(player.getUUID());
                                    source.sendSystemMessage(Component.literal("Added " + player.getName() + " as an Intruder to " + dimensionId));


                                    return Command.SINGLE_SUCCESS;
                                }))
                ));

        dispatcher.register(Commands.literal("paradiseWhitelists")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("operation", StringArgumentType.word()).suggests((ctx, builder) -> {
                            builder.suggest("list");
                            builder.suggest("add");
                            builder.suggest("remove");
                            builder.suggest("flip");
                            builder.suggest("check");
                            return builder.buildFuture();
                        })
                            .then(Commands.argument("dimension", ResourceLocationArgument.id()).suggests(DIMENSION_SUGGESTIONS)
                                    .executes((context) -> {
                                        // List whitelist data
                                        CommandSourceStack source = context.getSource();

                                        if (!source.isPlayer()) {
                                            source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        String operation = StringArgumentType.getString(context, "operation");
                                        if (!operation.equals("list")) {
                                            source.sendFailure(Component.literal("Non-list operations require a target player").withStyle(ChatFormatting.RED));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        MinecraftServer server = source.getServer();
                                        ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");

                                        WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());
                                        Map<String, Long> activeWhitelist = whitelistsSavedData.getActive(dimensionId);
                                        if (!activeWhitelist.isEmpty()) {
                                            source.sendSystemMessage(Component.literal("Active members of " + dimensionId + "'s whitelist:"));
                                            for (Map.Entry<String, Long> entry : activeWhitelist.entrySet()) {
                                                Date date = new Date(entry.getValue());
                                                source.sendSystemMessage(Component.literal(" - " + entry.getKey() + ", " + date).withStyle(ChatFormatting.YELLOW));
                                            }
                                        }

                                        Set<String> inactiveWhitelist = whitelistsSavedData.getHistory(dimensionId);
                                        if (!inactiveWhitelist.isEmpty()) {
                                            source.sendSystemMessage(Component.literal("Inactive members of " + dimensionId + "'s whitelist:"));
                                            for (String entry : inactiveWhitelist) {
                                                source.sendSystemMessage(Component.literal(" - " + entry).withStyle(ChatFormatting.YELLOW));
                                            }
                                        }

                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                        context.getSource().getServer().getPlayerList().getPlayers()
                                                                .forEach(p -> builder.suggest(p.getName().getString()));
                                                        return builder.buildFuture();
                                                    })
                                            .executes((context) -> {
                                                // Other operations
                                                CommandSourceStack source = context.getSource();
                                                MinecraftServer server = source.getServer();

                                                if (!source.isPlayer()) {
                                                    source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                                    return Command.SINGLE_SUCCESS;
                                                }

                                                String operation = StringArgumentType.getString(context, "operation");
                                                String playerName = StringArgumentType.getString(context, "player");
                                                ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");

                                                ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));

                                                ServerPlayer player = level.players().stream()
                                                        .filter(p -> p.getName().getString().equals(playerName))
                                                        .findFirst().orElse(null);

                                                WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());
                                                IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(level);

                                                switch(operation) {
                                                    case "add":
                                                        source.sendSystemMessage(Component.literal("Added " + playerName + " to active list in " + dimensionId));
                                                        whitelistsSavedData.addPlayer(dimensionId, playerName);
                                                        if (player != null) intrudersSavedData.remove(player.getUUID());
                                                        break;
                                                    case "remove":
                                                        source.sendSystemMessage(Component.literal("Removed " + playerName + " from either list if they were present in " + dimensionId));
                                                        whitelistsSavedData.removePlayer(dimensionId, playerName);
                                                        break;
                                                    case "flip":
                                                        if (whitelistsSavedData.flipPlayer(dimensionId, playerName)) {
                                                            if (player != null) intrudersSavedData.remove(player.getUUID());
                                                        }
                                                        source.sendSystemMessage(Component.literal("Moved " + playerName + " to the opposite list in " + dimensionId));
                                                        break;
                                                    case "check":
                                                        source.sendSystemMessage(Component.literal(playerName + ((whitelistsSavedData.isWhitelisted(dimensionId, playerName)) ? " is " : " isn't ") + "whitelisted in " + dimensionId));
                                                        break;
                                                    default:
                                                        source.sendFailure(Component.literal("Invalid operation, or called list with a target").withStyle(ChatFormatting.RED));
                                                        break;
                                                }

                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                        )
        );


        // Remove dimension
        dispatcher.register(Commands.literal("paradiseRemoveDimension")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("dimension", ResourceLocationArgument.id()).suggests(DIMENSION_SUGGESTIONS)
                        .then(Commands.argument("replaceDataServer?", BoolArgumentType.bool())
                            .executes((context) -> {
                                CommandSourceStack source = context.getSource();
                                MinecraftServer server = source.getServer();

                                if (!source.isPlayer()) {
                                    source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                    return Command.SINGLE_SUCCESS;
                                }

                                ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");

                                if (dimensionId.getPath().equals("nullspace")) {
                                    source.sendFailure(Component.literal("YOU CANNOT DELETE NULLSPACE").withStyle(ChatFormatting.RED));
                                    return Command.SINGLE_SUCCESS;
                                }

                                // Remove / Replace data_server
                                DataServerLocations dsl = DataServerLocations.getOrCreate(server.overworld());
                                Pair<BlockPos, ResourceLocation> dataServerLocation = dsl.get(dimensionId);
                                if (dataServerLocation != null && dataServerLocation.getLeft() != null && dataServerLocation.getRight() != null) {
                                    ServerLevel dataServerLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, dataServerLocation.getRight()));

                                    if (dataServerLevel != null && dataServerLevel.getBlockState(dataServerLocation.getLeft()).is(ModBlocks.DATA_SERVER)) {
                                        if (BoolArgumentType.getBool(context,"replaceDataServer?")) {
                                            dataServerLevel.setBlock(dataServerLocation.getLeft(), Blocks.AIR.defaultBlockState(), 3);

                                            dataServerLevel.setBlock(dataServerLocation.getLeft(), ModBlocks.DATA_SERVER.defaultBlockState(), 3);
                                            DataServerBlockEntity dsbe = dataServerLevel.getBlockEntity(dataServerLocation.getLeft(), ModBlockEntities.DATA_SERVER).orElse(null);
                                            if (dsbe != null) {
                                                ResourceLocation id = dsbe.getDimension();
                                                source.sendSystemMessage(Component.literal("Created new Data Server with dimension: ").append(Component.literal(id.toString()).withStyle(ChatFormatting.AQUA)));
                                            }
                                        } else {
                                            dataServerLevel.setBlock(dataServerLocation.getLeft(), Blocks.AIR.defaultBlockState(), 3);
                                        }
                                    }
                                }

                                ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
                                ServerLevel levelToDelete = server.getLevel(dimensionKey);
                                if (levelToDelete == null) {
                                    source.sendFailure(Component.literal("Unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                    return Command.SINGLE_SUCCESS;
                                }

                                ResourceLocation id = dimensionKey.location();
                                if (!DimensionRegistry.from(context.getSource().getServer()).canDeleteDimension(dimensionKey)) {
                                    throw CANNOT_REMOVE.create();
                                }

                                // Remove whitelist
                                WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());
                                whitelistsSavedData.deleteWhitelist(id);

                                DimensionRegistry.from(context.getSource().getServer()).deleteDynamicDimension(id, null);
                                return Command.SINGLE_SUCCESS;
                            }))));

        // Create Data Server
        dispatcher.register(Commands.literal("paradiseGenDataServer")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("dataServerLocation", BlockPosArgument.blockPos())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            BlockPos targetPos = BlockPosArgument.getBlockPos(context, "dataServerLocation");
                            ServerLevel serverLevel = source.getPlayer().serverLevel();
                            serverLevel.setBlock(targetPos, ModBlocks.DATA_SERVER.defaultBlockState(), 3);
                            DataServerBlockEntity dsbe = serverLevel.getBlockEntity(targetPos, ModBlockEntities.DATA_SERVER).orElse(null);
                            if (dsbe == null) {
                                source.sendFailure(Component.literal("Failed to fetch DataServer block entity").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                            ResourceLocation id = dsbe.getDimension();
                            source.sendSystemMessage(Component.literal("Created new Data Server with dimension: ").append(Component.literal(id.toString()).withStyle(ChatFormatting.AQUA)));
                            return Command.SINGLE_SUCCESS;
                        })));

        // Create dimension
        dispatcher.register(Commands.literal("paradiseGenDimension")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word()).suggests((
                        (context, builder) -> {
                            builder.suggest("NIGHT");
                            builder.suggest("DAY");
                            return builder.buildFuture();
                        }))
                        .executes((context -> {

                            CommandSourceStack source = context.getSource();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            String typeArgument = StringArgumentType.getString(context, "type");
                            try {
                                DimensionRegistry.ParadiseType type = DimensionRegistry.ParadiseType.valueOf(typeArgument);
                                ResourceLocation id = ((DimensionProvider) source.getServer()).paradise$createIfAbsent(type);

                                source.sendSystemMessage(Component.literal("Created new PARADISE_" + typeArgument + " dimension: ").append(Component.literal(id.toString()).withStyle(ChatFormatting.AQUA)));

                                return Command.SINGLE_SUCCESS;
                            } catch (Exception e) {
                                source.sendFailure(Component.literal("Dimension type not recognized").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                        }))));

        // Disc creator
        dispatcher.register(Commands.literal("paradiseGenDisc")
                        .requires(s -> s.hasPermission(2))
                .then(Commands.argument("dimension", ResourceLocationArgument.id()).suggests(DIMENSION_SUGGESTIONS)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");
                            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);

                            MinecraftServer server = source.getServer();
                            ServerLevel targetLevel = server.getLevel(dimensionKey);

                            if (targetLevel == null) {
                                source.sendFailure(Component.literal("Unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            ServerPlayer player = source.getPlayerOrException();

                            ItemStack toGive = new ItemStack(ModItems.ACCESS_DISC, 1);
                            toGive.set(ModComponents.DIMENSION_ADDRESS, new ModComponents.DimensionAddressComponent(dimensionId));

                            boolean flag = player.getInventory().add(toGive);
                            if (flag) {
                                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                                player.containerMenu.broadcastChanges();
                            } else {
                                ItemEntity itementity = player.drop(toGive, false);
                                if (itementity != null) {
                                    itementity.setNoPickUpDelay();
                                    itementity.setTarget(player.getUUID());
                                }
                            }

                            return Command.SINGLE_SUCCESS;
                        })));


        // Easy teleport
        dispatcher.register(Commands.literal("paradiseTp")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("dimension", ResourceLocationArgument.id()).suggests(DIMENSION_SUGGESTIONS)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            MinecraftServer server = source.getServer();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");
                            DataServerLocations dsl = DataServerLocations.getOrCreate(server.overworld());
                            Pair<BlockPos, ResourceLocation> dataServerLocation = dsl.get(dimensionId);

                            if (dataServerLocation != null && dataServerLocation.getLeft() != null && dataServerLocation.getRight() != null) {
                                ServerPlayer player = source.getPlayerOrException();
                                ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dataServerLocation.getRight());
                                ServerLevel targetLevel = server.getLevel(dimensionKey);

                                if (targetLevel == null) {
                                    source.sendFailure(Component.literal("DataServer in unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                    return Command.SINGLE_SUCCESS;
                                }

                                Vec3 targetPos = dataServerLocation.getLeft().relative(Direction.NORTH).getBottomCenter();

                                player.teleportTo(
                                        targetLevel,
                                        targetPos.x,
                                        targetPos.y,
                                        targetPos.z,
                                        player.getYRot(),
                                        player.getXRot()
                                );
                                return Command.SINGLE_SUCCESS;
                            } else {
                                source.sendFailure(Component.literal("Can't find DataServer associated with " + dimensionId).withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }


                        })
                        .then(Commands.argument("location", Vec3Argument.vec3())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();

                                    if (!source.isPlayer()) {
                                        source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    ResourceLocation dimensionId = ResourceLocationArgument.getId(context, "dimension");
                                    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);

                                    MinecraftServer server = source.getServer();
                                    ServerLevel targetLevel = server.getLevel(dimensionKey);

                                    if (targetLevel == null) {
                                        source.sendFailure(Component.literal("Unknown dimension: " + dimensionId).withStyle(ChatFormatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Vec3 targetPos = Vec3Argument.getVec3(context, "location");
                                    ServerPlayer player = source.getPlayerOrException();

                                    player.teleportTo(
                                            targetLevel,
                                            targetPos.x,
                                            targetPos.y,
                                            targetPos.z,
                                            player.getYRot(),
                                            player.getXRot()
                                    );

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
