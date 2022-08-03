package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BossCapMessage implements IMessage
{
    BossCapability cap = null;

    private int         entityID = -1;
    private CompoundTag nbt      = null;

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
    public void handle(final Supplier<NetworkEvent.Context> contextSupplier)
    {
        if (contextSupplier.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
        {
            BrutalBosses.LOGGER.error("Boss capability message sent to the wrong side!", new Exception());
        }
        else
        {
            Minecraft.getInstance().execute(() ->
            {
                final Entity entity = Minecraft.getInstance().player.level.getEntity(entityID);
                if (entity != null)
                {
                    entity.getCapability(BossCapability.BOSS_CAP).orElse(null).deserializeNBT(nbt);
                }
            });
        }

        contextSupplier.get().setPacketHandled(true);
    }
}
