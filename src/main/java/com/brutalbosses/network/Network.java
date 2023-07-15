package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class Network {

    public static final Network instance = new Network();

    private Network() {
        registerMessages();
    }

    public void registerMessages() {
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

    private Runnable catchErrorsFor(final Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                BrutalBosses.LOGGER.warn("error during packet:", e);
            }
        };
    }

    public void sendPacket(final ServerPlayer player, final IMessage msg) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        msg.write(buf);
        ServerPlayNetworking.send(player, msg.getID(), buf);
    }
}
