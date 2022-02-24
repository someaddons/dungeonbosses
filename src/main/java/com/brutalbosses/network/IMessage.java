package com.brutalbosses.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage
{
    void write(FriendlyByteBuf buffer);

    IMessage read(FriendlyByteBuf buffer);

    void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
