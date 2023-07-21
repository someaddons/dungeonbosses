package com.brutalbosses.mixin;

import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Projectile.class)
public abstract class ProjectileHitActionMixin extends Entity implements IOnProjectileHit
{
    private long maxLifeTime = 0;

    public ProjectileHitActionMixin(final EntityType<?> p_i48580_1_, final Level p_i48580_2_)
    {
        super(p_i48580_1_, p_i48580_2_);
    }

    private Consumer<HitResult> onHitAction = null;

    private float damageModifier = 0f;

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    public void onHitCallback(final HitResult rayTraceResult, final CallbackInfo ci)
    {
        if (onHitAction != null)
        {
            onHitAction.accept(rayTraceResult);
            onHitAction = null;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(final CallbackInfo ci)
    {
        if (maxLifeTime != 0 && level().getGameTime() > maxLifeTime)
        {
            remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean save(final CompoundTag nbt)
    {
        if (maxLifeTime != 0)
        {
            return false;
        }
        return super.save(nbt);
    }

    @Override
    public void setOnHitAction(final Consumer<HitResult> action)
    {
        onHitAction = action;
    }

    @Override
    public void setAddDamage(final float modifier)
    {
        damageModifier = modifier;
    }

    @Override
    public float getAddDamage()
    {
        return damageModifier;
    }

    @Override
    public void setMaxLifeTime(final long maxLifeTime)
    {
        this.maxLifeTime = maxLifeTime;
    }
}
