package com.brutalbosses.mixin;

import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileHitActionMixin extends Entity implements IOnProjectileHit
{
    private long maxLifeTime = 0;

    public ProjectileHitActionMixin(final EntityType<?> p_i48580_1_, final World p_i48580_2_)
    {
        super(p_i48580_1_, p_i48580_2_);
    }

    private Consumer<RayTraceResult> onHitAction = null;

    private float damageModifier = 0f;

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    public void onHitCallback(final RayTraceResult rayTraceResult, final CallbackInfo ci)
    {
        if (onHitAction != null)
        {
            onHitAction.accept(rayTraceResult);
            onHitAction = null;
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        if (maxLifeTime != 0 && level.getGameTime() > maxLifeTime)
        {
            remove();
        }
    }

    @Override
    public boolean save(final CompoundNBT nbt)
    {
        if (maxLifeTime != 0)
        {
            return false;
        }
        return super.save(nbt);
    }

    @Override
    public void setOnHitAction(final Consumer<RayTraceResult> action)
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
