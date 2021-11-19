package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomEntityRenderData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity>
{
    @Shadow
    protected abstract void scale(final T p_225620_1_, final MatrixStack p_225620_2_, final float p_225620_3_);

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingRenderer;scale(Lnet/minecraft/entity/LivingEntity;Lcom/mojang/blaze3d/matrix/MatrixStack;F)V"))
    private void on(final LivingRenderer livingRenderer, final T entity, final MatrixStack stack, final float value)
    {
        if (entity instanceof CustomEntityRenderData)
        {
            final float scale = ((CustomEntityRenderData) entity).getVisualScale();
            stack.scale(scale, scale, scale);
        }

        this.scale(entity, stack, value);
    }
}
