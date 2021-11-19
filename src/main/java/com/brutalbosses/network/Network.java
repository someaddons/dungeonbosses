package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Network
{
    private static final String        PROTOCOL_VERSION = "1";
    private final        SimpleChannel channel;

    public static final Network instance = new Network();

    private Network()
    {
        channel = NetworkRegistry.newSimpleChannel(
          new ResourceLocation(BrutalBosses.MODID, "main"),
          () -> PROTOCOL_VERSION,
          e -> true,
          e -> true
        );
    }

    public void registerMessages()
    {
        channel.registerMessage(1, BossCapMessage.class,
          BossCapMessage::write,
          p -> {
              final BossCapMessage msg = new BossCapMessage();
              msg.read(p);
              return msg;
          }, BossCapMessage::handle);
        channel.registerMessage(2, BossOverlayMessage.class,
          BossOverlayMessage::write,
          p -> {
              final BossOverlayMessage msg = new BossOverlayMessage();
              msg.read(p);
              return msg;
          }, BossOverlayMessage::handle);
    }

    public void sendPacket(final ServerPlayerEntity playerEntity, final IMessage msg)
    {
        channel.send(PacketDistributor.PLAYER.with(() -> playerEntity), msg);
    }
}
