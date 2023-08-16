package com.brutalbosses.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class Network
{

    public static final Network instance = new Network();

    private Network()
    {

    }

    public void sendPacket(final ServerPlayer player, final IMessage msg)
    {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        msg.write(buf);
        ServerPlayNetworking.send(player, msg.getID(), buf);
    }
}
