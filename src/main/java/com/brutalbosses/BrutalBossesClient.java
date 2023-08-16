package com.brutalbosses;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.CSpriteRenderer;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.BossTypeSyncMessage;
import com.brutalbosses.network.VanillaParticleMessage;
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

        ClientPlayNetworking.registerGlobalReceiver(BossCapMessage.ID, (client, handler, buf, responseSender) -> {
            final BossCapMessage message = new BossCapMessage().read(buf);
            client.execute(catchErrorsFor(() -> {
                // Everything in this lambda is run on the render thread
                message.handle(handler, client);
            }));
        });

        ClientPlayNetworking.registerGlobalReceiver(BossOverlayMessage.ID, (client, handler, buf, responseSender) -> {
            BossOverlayMessage message = new BossOverlayMessage().read(buf);
            client.execute(catchErrorsFor(() -> {
                // Everything in this lambda is run on the render thread
                message.handle(handler, client);
            }));
        });

        ClientPlayNetworking.registerGlobalReceiver(BossTypeSyncMessage.ID, (client, handler, buf, responseSender) -> {
            BossTypeSyncMessage message = new BossTypeSyncMessage().read(buf);
            client.execute(catchErrorsFor(() -> {
                // Everything in this lambda is run on the render thread
                message.handle(handler, client);
            }));
        });

        ClientPlayNetworking.registerGlobalReceiver(VanillaParticleMessage.ID, (client, handler, buf, responseSender) -> {
            VanillaParticleMessage message = new VanillaParticleMessage().read(buf);
            client.execute(catchErrorsFor(() -> {
                // Everything in this lambda is run on the render thread
                message.handle(handler, client);
            }));
        });
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
