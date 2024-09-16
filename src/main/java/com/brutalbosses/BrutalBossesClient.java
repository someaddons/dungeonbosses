package com.brutalbosses;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.CSpriteRenderer;
import com.brutalbosses.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class BrutalBossesClient implements ClientModInitializer
{

    @Override
    public void onInitializeClient()
    {
        EntityRendererRegistry.register(ModEntities.THROWN_ITEMC, manager -> new CSpriteRenderer(manager, Minecraft.getInstance().getItemRenderer(), 1.0f, true));
        ClientPlayNetworking.registerGlobalReceiver(BossCapMessage.TYPE,
          (msg, context) -> context.client().execute(catchErrorsFor(() -> ClientMessageHandler.handle(msg, context.client()))));
        ClientPlayNetworking.registerGlobalReceiver(BossOverlayMessage.TYPE,
          (msg, context) -> context.client().execute(catchErrorsFor(() -> ClientMessageHandler.handle(msg, context.client()))));
        ClientPlayNetworking.registerGlobalReceiver(BossTypeSyncMessage.TYPE,
          (msg, context) -> context.client().execute(catchErrorsFor(() -> ClientMessageHandler.handle(msg, context.client()))));
        ClientPlayNetworking.registerGlobalReceiver(VanillaParticleMessage.TYPE,
          (msg, context) -> context.client().execute(catchErrorsFor(() -> ClientMessageHandler.handle(msg, context.client()))));
    }

    private Runnable catchErrorsFor(final Runnable runnable)
    {
        return () -> {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                BrutalBosses.LOGGER.warn("error during packet:", e);
            }
        };
    }
}
