package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomEntityRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity>
{

    @Shadow
    protected abstract void scale(final T p_115314_, final PoseStack p_115315_, final float p_115316_);

    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void on(final LivingEntityRenderer livingRenderer, final T entity, final PoseStack stack, final float value)
    {
        if (entity instanceof CustomEntityRenderData)
        {
            final float scale = ((CustomEntityRenderData) entity).getVisualScale();
            stack.scale(scale, scale, scale);
        }

        this.scale(entity, stack, value);
    }
}
