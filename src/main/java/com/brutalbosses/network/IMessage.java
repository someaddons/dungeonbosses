package com.brutalbosses.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IMessage {

    void write(FriendlyByteBuf buffer);

    IMessage read(FriendlyByteBuf buffer);

    void handle(ClientPacketListener handler, Minecraft client);

    public ResourceLocation getID();
}
