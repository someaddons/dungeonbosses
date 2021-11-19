package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomEntityRenderData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class EntityRenderDataMixin implements CustomEntityRenderData
{
    @Shadow
    private EntitySize dimensions;
    private float      visualScale = 1.0f;

    @Override
    public float getVisualScale()
    {
        return visualScale;
    }

    @Override
    public void setVisualScale(final float scale)
    {
        this.visualScale = scale;
    }

    @Override
    public void setDimension(final EntitySize size)
    {
        dimensions = size;
    }
}
