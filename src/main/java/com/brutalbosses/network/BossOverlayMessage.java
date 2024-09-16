package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * fake message for UI
 */
public class BossOverlayMessage implements IMessage, CustomPacketPayload
{
    public static final CustomPacketPayload.Type<BossOverlayMessage> TYPE     =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrutalBosses.MOD_ID, "bossoverlay"));
    public int entityID = -1;

    public BossOverlayMessage(final int entityID)
    {
        this.entityID = entityID;
    }

    public BossOverlayMessage()
    {
        // Deserial
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(entityID);
    }

    @Override
    public BossOverlayMessage read(final FriendlyByteBuf buffer)
    {
        entityID = buffer.readInt();
        return this;
    }

    @Override
    public ResourceLocation getID()
    {
        return TYPE.id();
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}

