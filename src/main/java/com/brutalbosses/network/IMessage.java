package com.brutalbosses.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage
{
    void write(PacketBuffer buffer);

    IMessage read(PacketBuffer buffer);

    void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
