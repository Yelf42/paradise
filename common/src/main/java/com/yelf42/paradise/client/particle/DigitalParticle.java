package com.yelf42.paradise.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

public class DigitalParticle extends TextureSheetParticle {
    static final RandomSource RANDOM = RandomSource.create();

    public DigitalParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet sprites) {
        super(level, x, y, z);

        this.rCol = (float) r;
        this.gCol = (float) g;
        this.bCol = (float) b;

        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.setSprite(sprites.get(RANDOM));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float partialTick) {
        return 240;
    }

    public void tick() {
        super.tick();
//        if (RANDOM.nextInt(10) == 0) {
//            this.setSprite(sprites.get(RANDOM));
//        }
        float f = 1.0f - ((float)this.age) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        this.alpha = f;
    }

    public static class BitFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        private static final Vector3f COLOR = new Vector3f(155/ 255.0f, 233/ 255.0f, 1.0f);

        public BitFactory(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            DigitalParticle digitalParticle = new DigitalParticle(level, x, y, z, COLOR.x, COLOR.y, COLOR.z, this.sprite);

            digitalParticle.yd *= 0.2F;
            if (xSpeed == (double)0.0F && zSpeed == (double)0.0F) {
                digitalParticle.xd *= 0.1F;
                digitalParticle.zd *= 0.1F;
            }

            digitalParticle.setLifetime(12);
            return digitalParticle;
        }
    }

    public static class AscendingBitFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        private static final Vector3f COLOR = new Vector3f(155/ 255.0f, 233/ 255.0f, 1.0f);

        public AscendingBitFactory(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            DigitalParticle digitalParticle = new DigitalParticle(level, x, y, z, COLOR.x, COLOR.y, COLOR.z, this.sprite);
            digitalParticle.yd = ySpeed;

            digitalParticle.setLifetime(RANDOM.nextInt(24, 48));
            return digitalParticle;
        }
    }
}
