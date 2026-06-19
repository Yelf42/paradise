package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.renderer.ModClientModels;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Models registered here are used in {@link com.yelf42.paradise.mixin.ItemRendererMixin}
 * <p>
 * ModelResourceLocation used here and in {@link com.yelf42.paradise.mixin.ItemRendererMixin} should be
 * defined in {@link com.yelf42.paradise.client.renderer.ModClientModels} (public variables aren't definable in mixins)
 */

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    protected abstract void loadSpecialItemModelAndDependencies(ModelResourceLocation modelLocation);

    @Final
    @Shadow
    private Map<ModelResourceLocation, UnbakedModel> topLevelModels;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(BlockColors blockColors, ProfilerFiller profilerFiller,
                        Map<ResourceLocation, BlockModel> modelResources,
                        Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources,
                        CallbackInfo ci) {

        for (ModelResourceLocation modelResourceLocation : ModClientModels.CUSTOM_GUI_MODEL_LOCATIONS) {
            loadSpecialItemModelAndDependencies(modelResourceLocation);
            UnbakedModel model = topLevelModels.get(modelResourceLocation);
            if (model != null) {
                model.resolveParents(this::getModel);
            }
        }
    }

    @Shadow
    UnbakedModel getModel(ResourceLocation location) { return null; }
}
