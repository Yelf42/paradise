package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.config.ParadiseServerHolder;
import com.yelf42.paradise.dimensions.BunkerSavedData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void paradise$limitBunkerCount(
            StructureSet.StructureSelectionEntry structureSelectionEntry,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState random,
            StructureTemplateManager structureTemplateManager,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!structureSelectionEntry.structure().is(Paradise.identifier("bunker"))) {
            return;
        }

        MinecraftServer server = ParadiseServerHolder.get();
        if (server == null || BunkerSavedData.get(server).getBunkersGenerated() >= Paradise.CONFIG.bunkerCount) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tryGenerateStructure", at = @At("RETURN"))
    private void paradise$countBunkerGeneration(
            StructureSet.StructureSelectionEntry structureSelectionEntry,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState random,
            StructureTemplateManager structureTemplateManager,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (structureSelectionEntry.structure().is(Paradise.identifier("bunker")) && cir.getReturnValueZ()) {
            MinecraftServer server = ParadiseServerHolder.get();
            if (server != null) {
                BunkerSavedData.get(server).incrementBunkersGenerated();
            }
        }
    }
}
