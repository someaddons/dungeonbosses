package com.brutalbosses;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.CSpriteRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class BrutalBossesClient implements ClientModInitializer
{

    @Override
    public void onInitializeClient()
    {
        EntityRendererRegistry.register(ModEntities.THROWN_ITEMC, manager -> new CSpriteRenderer(manager, Minecraft.getInstance().getItemRenderer(), 1.0f, true));
    }
}
