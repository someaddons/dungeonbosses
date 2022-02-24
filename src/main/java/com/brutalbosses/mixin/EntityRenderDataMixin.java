package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomEntityRenderData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class EntityRenderDataMixin implements CustomEntityRenderData
{
    @Shadow
    private EntityDimensions dimensions;
    private float            visualScale = 1.0f;

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
    public void setDimension(final EntityDimensions size)
    {
        dimensions = size;
    }
}
