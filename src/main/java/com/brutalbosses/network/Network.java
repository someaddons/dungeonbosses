package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
        channel.registerMessage(3, VanillaParticleMessage.class,
          VanillaParticleMessage::write,
          p -> {
              final VanillaParticleMessage msg = new VanillaParticleMessage();
              msg.read(p);
              return msg;
          }, VanillaParticleMessage::handle);
        channel.registerMessage(4, BossTypeSyncMessage.class,
          BossTypeSyncMessage::write,
          p -> {
              final BossTypeSyncMessage msg = new BossTypeSyncMessage();
              msg.read(p);
              return msg;
          }, BossTypeSyncMessage::handle);
    }

    public void sendPacket(final ServerPlayer Player, final IMessage msg)
    {
        channel.send(PacketDistributor.PLAYER.with(() -> Player), msg);
    }
}
