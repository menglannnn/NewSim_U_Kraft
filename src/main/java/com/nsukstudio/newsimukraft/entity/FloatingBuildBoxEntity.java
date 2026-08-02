package com.nsukstudio.newsimukraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * 悬浮建筑盒实体
 *
 * 所属系统：建筑
 * 特性：
 *   - 无物理碰撞，悬浮循环上下运动 + Y轴旋转动画
 *   - 免疫一切伤害（爆炸/燃烧/攻击）
 *   - 无法被点燃，无法被推动
 *   - 动画参数（幅度/速度）通过同步数据持久化
 */
public class FloatingBuildBoxEntity extends PathfinderMob {

    private static final EntityDataAccessor<Float> DATA_FLOAT_HEIGHT =
            SynchedEntityData.defineId(FloatingBuildBoxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FLOAT_SPEED =
            SynchedEntityData.defineId(FloatingBuildBoxEntity.class, EntityDataSerializers.FLOAT);

    private int floatTimer = 0;
    private double baseY;

    public FloatingBuildBoxEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLOAT_HEIGHT, 0.5f);
        builder.define(DATA_FLOAT_SPEED, 0.02f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount == 1) {
            this.baseY = this.getY();
        }

        if (this.level().isClientSide()) {
            handleFloatingAnimation();
        } else {
            this.setYRot(this.getYRot() + 1.0f);
            this.setInvulnerable(true);
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.clearFire();
        this.setRemainingFireTicks(0);
    }

    private void handleFloatingAnimation() {
        floatTimer++;
        float animationSpeed = Math.max(0.01f, this.getFloatSpeed() * 5.0f);
        float floatOffset = (float) Math.sin(floatTimer * animationSpeed) * (this.getFloatHeight() * 0.1f);
        this.setYRot(this.getYRot() + 2.0f);
        this.setPos(this.getX(), this.baseY + floatOffset, this.getZ());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("FloatHeight", this.getFloatHeight());
        output.putFloat("FloatSpeed", this.getFloatSpeed());
        output.putDouble("BaseY", this.baseY);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setFloatHeight(input.getFloatOr("FloatHeight", 0.5f));
        this.setFloatSpeed(input.getFloatOr("FloatSpeed", 0.02f));
        this.baseY = input.getDoubleOr("BaseY", this.getY());
        this.setInvulnerable(true);
    }

    public float getFloatHeight() { return this.entityData.get(DATA_FLOAT_HEIGHT); }
    public void setFloatHeight(float h) { this.entityData.set(DATA_FLOAT_HEIGHT, h); }

    public float getFloatSpeed() { return this.entityData.get(DATA_FLOAT_SPEED); }
    public void setFloatSpeed(float s) { this.entityData.set(DATA_FLOAT_SPEED, s); }
}
