package com.brutalbosses.entity.thrownentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Similar to SpriteRenderer
 *
 * @param <T>
 */
public class CSpriteRenderer<T extends ThrownItemEntity & IRendersAsItem> extends EntityRenderer<T>
{
    private final ItemRenderer itemRenderer;
    private       boolean      fullBright = false;

    public CSpriteRenderer(EntityRendererManager entityRendererManager, ItemRenderer itemRenderer, float scale, boolean fullbright)
    {
        super(entityRendererManager);
        this.itemRenderer = itemRenderer;
        this.fullBright = fullbright;
    }

    public CSpriteRenderer(EntityRendererManager entityRendererManager, ItemRenderer itemRenderer)
    {
        this(entityRendererManager, itemRenderer, 1.0F, false);
    }

    protected int getBlockLightLevel(T entity, BlockPos pos)
    {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, pos);
    }

    public void render(T entity, float float1, float float2, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int intvalue)
    {
        if (entity.tickCount >= 2 || this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) >= 12.25D)
        {
            matrixStack.pushPose();
            // Takes scale from the thrown entity
            matrixStack.scale(entity.getScale(), entity.getScale(), entity.getScale());
            matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            this.itemRenderer.renderStatic(((IRendersAsItem) entity).getItem(),
              ItemCameraTransforms.TransformType.GROUND,
              intvalue,
              OverlayTexture.NO_OVERLAY,
              matrixStack,
              iRenderTypeBuffer);
            matrixStack.popPose();
            super.render(entity, float1, float2, matrixStack, iRenderTypeBuffer, intvalue);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownItemEntity p_110775_1_)
    {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
