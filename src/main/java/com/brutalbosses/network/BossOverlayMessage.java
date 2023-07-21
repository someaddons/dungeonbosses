package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * fake message for UI
 */
public class BossOverlayMessage implements IMessage
{
    private int entityID = -1;

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
    public void handle(final Supplier<NetworkEvent.Context> contextSupplier)
    {
        if (contextSupplier.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
        {
            BrutalBosses.LOGGER.error("Boss Overlay message sent to the wrong side!", new Exception());
        }
        else
        {
            final Entity entity = Minecraft.getInstance().player.level().getEntity(entityID);
            if (entity != null)
            {
                ClientEventHandler.checkEntity(entity);
            }
        }

        contextSupplier.get().setPacketHandled(true);
    }
}
