package com.brutalbosses.entity;

import net.minecraft.util.math.RayTraceResult;

import java.util.function.Consumer;

public interface IOnProjectileHit
{
    public void setOnHitAction(final Consumer<RayTraceResult> action);

    public void setAddDamage(final float modifier);

    public float getAddDamage();

    public void setMaxLifeTime(final long maxLifeTime);
}
