package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Message for vanilla particles around a citizen, in villager-like shape.
 */
public class VanillaParticleMessage implements IMessage, CustomPacketPayload
{
    public static final CustomPacketPayload.Type<VanillaParticleMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrutalBosses.MOD_ID, "particlemsg"));
    /**
     * Citizen Position
     */
    public              double                                           x;
    public              double                                           y;
    public              double                                           z;

    /**
     * Particle id
     */
    public SimpleParticleType type;

    public VanillaParticleMessage()
    {
        super();
    }

    public VanillaParticleMessage(final double x, final double y, final double z, final SimpleParticleType type)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeResourceLocation(BuiltInRegistries.PARTICLE_TYPE.getKey(this.type));
    }

    @Override
    public VanillaParticleMessage read(final FriendlyByteBuf buffer)
    {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        this.type = (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(buffer.readResourceLocation());
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
