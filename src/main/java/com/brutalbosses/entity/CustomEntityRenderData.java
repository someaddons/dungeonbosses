package com.brutalbosses.entity;

import net.minecraft.world.entity.EntityDimensions;

public interface CustomEntityRenderData
{
    public float getVisualScale();

    public void setVisualScale(final float scale);

    void setDimension(EntityDimensions size);
}
