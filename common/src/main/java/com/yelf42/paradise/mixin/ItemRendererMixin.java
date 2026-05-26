package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.renderer.ModClientModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * {@link com.yelf42.paradise.mixin.ModelBakeryMixin} registers the custom models
 * so it can be retrieved by {@link net.minecraft.client.resources.model.ModelManager#getModel}.
 * <p>
 * ModelResourceLocation used here and in {@link com.yelf42.paradise.mixin.ModelBakeryMixin} should be
 * defined in {@link com.yelf42.paradise.client.renderer.ModClientModels} (public variables aren't definable in mixins)
 */

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BakedModel modifyModel(BakedModel model, ItemStack stack, ItemDisplayContext ctx) {
        if (!ModClientModels.CUSTOM_GUI_MODELS.containsKey(stack.getItem())) return model;

        if (ctx == ItemDisplayContext.GUI
                || ctx == ItemDisplayContext.GROUND
                || ctx == ItemDisplayContext.FIXED) {
            return Minecraft.getInstance()
                    .getModelManager()
                    .getModel(ModClientModels.CUSTOM_GUI_MODELS.get(stack.getItem()));
        }

        return model;
    }
}
