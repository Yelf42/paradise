package com.yelf42.paradise.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RippleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double startingHeight;

    protected RippleParticle(ClientLevel level, double x, double y, double z, float r, float g, float b, SpriteSet sprites) {
        super(level, x, y, z);
        this.startingHeight = y;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.lifetime = 25;
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
        this.scale(4.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        double a = ((double) this.age) / (double) this.lifetime;
        this.y = this.startingHeight + (0.5f * Math.sqrt(a) * Math.pow(1.0f - a, 3.0f));
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        double a = ((double) this.age + partialTicks) / (double) this.lifetime;
        a = Mth.clamp(Math.pow(2.0f * a - 1.0f, 3.0f), 0.0F, 1.0F);
        this.alpha = (float) (1.0F - a);
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotateX((float) (-Math.PI / 2.0));
        this.renderRotatedQuad(buffer, renderInfo, quaternionf, partialTicks);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class DayFactory implements ParticleProvider {
        private final SpriteSet sprites;
        private static final Vector3f DAY_RIPPLE_COLOR = new Vector3f(50/ 255.0f, 120/ 255.0f, 1.0f);

        public DayFactory(SpriteSet sprite) {
            this.sprites = sprite;
        }

        public Particle createParticle(ParticleOptions particleOptions, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            RippleParticle rippleParticle = new RippleParticle(level, x, y, z, DAY_RIPPLE_COLOR.x, DAY_RIPPLE_COLOR.y, DAY_RIPPLE_COLOR.z, this.sprites);
            rippleParticle.setAlpha(1.0F);
            return rippleParticle;
        }
    }

    public static class NightFactory implements ParticleProvider {
        private final SpriteSet sprites;
        private static final Vector3f NIGHT_RIPPLE_COLOR = new Vector3f(33/ 255.0f, 33/ 255.0f, 35/ 255.0f);

        public NightFactory(SpriteSet sprite) {
            this.sprites = sprite;
        }

        public Particle createParticle(ParticleOptions particleOptions, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            RippleParticle rippleParticle = new RippleParticle(level, x, y, z, NIGHT_RIPPLE_COLOR.x, NIGHT_RIPPLE_COLOR.y, NIGHT_RIPPLE_COLOR.z, this.sprites);
            rippleParticle.setAlpha(1.0F);
            return rippleParticle;
        }
    }

    public static class ErrorFactory implements ParticleProvider {
        private final SpriteSet sprites;
        private static final Vector3f ERROR_RIPPLE_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

        public ErrorFactory(SpriteSet sprite) {
            this.sprites = sprite;
        }

        public Particle createParticle(ParticleOptions particleOptions, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            RippleParticle rippleParticle = new RippleParticle(level, x, y, z, ERROR_RIPPLE_COLOR.x, ERROR_RIPPLE_COLOR.y, ERROR_RIPPLE_COLOR.z, this.sprites);
            rippleParticle.setAlpha(1.0F);
            return rippleParticle;
        }
    }
}
