package com.brutalbosses.entity;

import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;

public interface IOnProjectileHit
{
    public void setOnHitAction(final Consumer<HitResult> action);

    public void setAddDamage(final float modifier);

    public float getAddDamage();

    public void setMaxLifeTime(final long maxLifeTime);
}
