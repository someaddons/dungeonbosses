package com.brutalbosses.entity;

import net.minecraft.entity.EntitySize;

public interface CustomEntityRenderData
{
    public float getVisualScale();

    public void setVisualScale(final float scale);

    void setDimension(EntitySize size);
}
