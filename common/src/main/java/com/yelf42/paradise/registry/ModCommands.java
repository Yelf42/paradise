package com.yelf42.paradise.registry;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.yelf42.paradise.blocks.DataSeverBlockEntity;
import com.yelf42.paradise.dimensions.DataServerLocations;
import com.yelf42.paradise.dimensions.DimensionProvider;
import com.yelf42.paradise.dimensions.DimensionRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

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
                                            DataSeverBlockEntity dsbe = dataServerLevel.getBlockEntity(dataServerLocation.getLeft(), ModBlockEntities.DATA_SERVER).orElse(null);
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
                            DataSeverBlockEntity dsbe = serverLevel.getBlockEntity(targetPos, ModBlockEntities.DATA_SERVER).orElse(null);
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
