package com.yelf42.paradise.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ModRenderTypes extends RenderType {

    private ModRenderTypes(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2) {
        super(s, v, m, i, b, b2, r, r2);
        throw new IllegalStateException("This class is not meant to be constructed!");
    }

    public static void setHologramShader(ShaderInstance shader) {
        hologramShader = shader;
    }
    public static ShaderInstance hologramShader;
    private static final ShaderStateShard RENDERTYPE_HOLOGRAM_SHADER = new ShaderStateShard(() -> hologramShader);
    public static Function<ResourceLocation, RenderType> HOLOGRAM = Util.memoize(ModRenderTypes::createHologram);
    private static RenderType createHologram(ResourceLocation texture) {
        try {
            Method create = RenderType.class.getDeclaredMethod("create", String.class, VertexFormat.class,
                    VertexFormat.Mode.class, int.class, boolean.class, boolean.class,
                    RenderType.CompositeState.class);
            create.setAccessible(true);

            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_HOLOGRAM_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);

            return (RenderType) create.invoke(null, "paradise_hologram", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, true, false, state);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create hologram render type", e);
        }
    }
}
