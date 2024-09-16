package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class BossCapMessage implements IMessage, CustomPacketPayload
{
    public static final CustomPacketPayload.Type<BossCapMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrutalBosses.MOD_ID, "bosscap"));
    BossCapability cap = null;

    public int         entityID = -1;
    public CompoundTag nbt      = null;

    public BossCapMessage(final BossCapability cap)
    {
        this.cap = cap;
    }

    public BossCapMessage()
    {
        // Deserial
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(cap.getEntity().getId());
        buffer.writeNbt((CompoundTag) cap.serializeNBT());
    }

    @Override
    public BossCapMessage read(final FriendlyByteBuf buffer)
    {
        entityID = buffer.readInt();
        nbt = buffer.readNbt();
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