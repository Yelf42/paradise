package com.yelf42.paradise.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

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

    public static void setShimmerShader(ShaderInstance shader) {
        shimmerShader = shader;
    }
    public static ShaderInstance shimmerShader;
    private static final ShaderStateShard RENDERTYPE_SHIMMER_SHADER = new ShaderStateShard(() -> shimmerShader);
    public static Function<ResourceLocation, RenderType> SHIMMER = Util.memoize(ModRenderTypes::createShimmer);
    private static RenderType createShimmer(ResourceLocation texture) {
        try {
            Method create = RenderType.class.getDeclaredMethod("create", String.class, VertexFormat.class,
                    VertexFormat.Mode.class, int.class, boolean.class, boolean.class,
                    RenderType.CompositeState.class);
            create.setAccessible(true);

            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_SHIMMER_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);

            return (RenderType) create.invoke(null, "paradise_shimmer", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, true, false, state);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shimmer render type", e);
        }
    }


    public static void setDigitalTeleportShader(ShaderInstance shader) {
        digitalTeleportShader = shader;
    }
    public static ShaderInstance digitalTeleportShader;
    private static final ShaderStateShard RENDERTYPE_DIGITAL_TELEPORT_SHADER = new ShaderStateShard(() -> digitalTeleportShader);
    public static Function<ResourceLocation, RenderType> DIGITAL_TELEPORT = Util.memoize(ModRenderTypes::createDigitalTeleport);
    private static RenderType createDigitalTeleport(ResourceLocation texture) {
        try {
            Method create = RenderType.class.getDeclaredMethod("create", String.class, VertexFormat.class,
                    VertexFormat.Mode.class, int.class, boolean.class, boolean.class,
                    RenderType.CompositeState.class);
            create.setAccessible(true);

            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_DIGITAL_TELEPORT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);

            return (RenderType) create.invoke(null, "paradise_digital_teleport", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, true, false, state);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create hologram render type", e);
        }
    }

    public static void setPixelizeShader(ShaderInstance shader) {
        pixelizeShader = shader;
    }
    public static ShaderInstance pixelizeShader;
    public static DynamicTexture screenCopyTexture;
    private static final ShaderStateShard RENDERTYPE_PIXELIZE_SHADER = new ShaderStateShard(() -> pixelizeShader);
    public static Function<ResourceLocation, RenderType> PIXELIZE = Util.memoize(ModRenderTypes::createPixelize);
    private static RenderType createPixelize(ResourceLocation texture) {
        try {
            Method create = RenderType.class.getDeclaredMethod("create", String.class, VertexFormat.class,
                    VertexFormat.Mode.class, int.class, boolean.class, boolean.class,
                    RenderType.CompositeState.class);
            create.setAccessible(true);

            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_PIXELIZE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);

            RenderType base = (RenderType) create.invoke(null, "paradise_pixelize", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, true, false, state);

            return new RenderType("paradise_pixelize", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, true, false,
                    () -> {
                        base.setupRenderState();
                        ensureScreenCopyTexture();

                        if (ModRenderTypes.screenCopyTexture != null) {
                            RenderSystem.bindTexture(ModRenderTypes.screenCopyTexture.getId());
                            Window window = Minecraft.getInstance().getWindow();
                            GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0,
                                    window.getWidth(), window.getHeight());
                            RenderSystem.setShaderTexture(1, ModRenderTypes.screenCopyTexture.getId());
                        }
                    },
                    () -> {
                        RenderSystem.depthMask(true);
                        base.clearRenderState();
                    }
            ) {};
        } catch (Exception e) {
            throw new RuntimeException("Failed to create pixelize render type", e);
        }
    }
    public static void ensureScreenCopyTexture() {
        Minecraft mc = Minecraft.getInstance();

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        boolean recreate = false;

        if (screenCopyTexture == null) {
            recreate = true;
        } else {
            NativeImage pixels = screenCopyTexture.getPixels();

            if (pixels == null ||
                    pixels.getWidth() != width ||
                    pixels.getHeight() != height) {

                screenCopyTexture.close();
                recreate = true;
            }
        }

        if (recreate) {
            screenCopyTexture = new DynamicTexture(width, height, false);

            RenderSystem.bindTexture(screenCopyTexture.getId());

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }
}
