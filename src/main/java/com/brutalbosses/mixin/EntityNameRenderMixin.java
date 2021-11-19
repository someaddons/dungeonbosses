package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomEntityRenderData;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
/**
 * Disabled, replaced by client side bb change
 */
public class EntityNameRenderMixin
{
    @Redirect(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBbHeight()F"))
    public float customScaledBB(final Entity entity)
    {
        if (entity instanceof CustomEntityRenderData)
        {
            return entity.getBbHeight() * ((CustomEntityRenderData) entity).getVisualScale();
        }

        return entity.getBbHeight();
    }
}
