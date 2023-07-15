package com.brutalbosses.entity.thrownentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ItemSupplier;

/**
 * Similar to SpriteRenderer
 *
 * @param <T>
 */
public class CSpriteRenderer<T extends ThrownItemEntity & ItemSupplier> extends EntityRenderer<T>
{
    private final ItemRenderer itemRenderer;
    private       boolean      fullBright = false;

    public CSpriteRenderer(EntityRendererProvider.Context entityRendererManager, ItemRenderer itemRenderer, float scale, boolean fullbright)
    {
        super(entityRendererManager);
        this.itemRenderer = itemRenderer;
        this.fullBright = fullbright;
    }

    public CSpriteRenderer(EntityRendererProvider.Context entityRendererManager, ItemRenderer itemRenderer)
    {
        this(entityRendererManager, itemRenderer, 1.0F, false);
    }

    protected int getBlockLightLevel(T entity, BlockPos pos)
    {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, pos);
    }

    public void render(T entity, float p_116086_, float p_116087_, PoseStack p_116088_, MultiBufferSource p_116089_, int p_116090_)
    {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D))
        {
            p_116088_.pushPose();
            p_116088_.scale(entity.getScale(), entity.getScale(), entity.getScale());
            p_116088_.mulPose(this.entityRenderDispatcher.cameraOrientation());
            p_116088_.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.GROUND, p_116090_, OverlayTexture.NO_OVERLAY, p_116088_, p_116089_, entity.getId());
            p_116088_.popPose();
            super.render(entity, p_116086_, p_116087_, p_116088_, p_116089_, p_116090_);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownItemEntity entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
