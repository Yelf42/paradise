package com.yelf42.paradise.entities;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModEntities;
import com.yelf42.paradise.registry.ModItems;
import com.yelf42.paradise.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DigitalArrow extends AbstractArrow {
    private int duration = 100;

    public DigitalArrow(EntityType<? extends DigitalArrow> entityType, Level level) {
        super(entityType, level);
    }

    public DigitalArrow(Level level, LivingEntity owner, ItemStack pickupItemStack, ItemStack firedFromWeapon) {
        super(ModEntities.DIGITAL_ARROW, owner, level, pickupItemStack, firedFromWeapon);
    }

    public DigitalArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, ItemStack firedFromWeapon) {
        super(ModEntities.DIGITAL_ARROW, x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.DIGITAL_ARROW);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Duration")) {
            this.duration = compound.getInt("Duration");
        }

    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Duration", this.duration);
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround) {
            this.level().addParticle(ModParticles.BITS, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F);
        }

        duration--;
        if (duration < 0) {
            this.discard();
        }

        Vec3 vec3 = this.getDeltaMovement();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)180.0F / (double)(float)Math.PI));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)180.0F / (double)(float)Math.PI));

        Vec3 start = this.position();
        Vec3 end = start.add(vec3);
        EntityHitResult hit = this.findHitEntity(start, end);
        if (hit != null) {
            this.hitTargetOrDeflectSelf(hit);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) {
            super.onHitEntity(result);
        }
    }

    @Override
    public boolean isNoPhysics() {
        return true;
    }

    @Override
    public boolean isCritArrow() {
        return false;
    }
}