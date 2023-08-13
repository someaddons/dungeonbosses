package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * fake message for UI
 */
public class BossOverlayMessage implements IMessage {

    public static final ResourceLocation ID = new ResourceLocation(BrutalBosses.MOD_ID, "bossoverlay");
    private int entityID = -1;

    public BossOverlayMessage(final int entityID) {
        this.entityID = entityID;
    }

    public BossOverlayMessage() {
        // Deserial
    }

    @Override
    public void write(final FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);
    }

    @Override
    public BossOverlayMessage read(final FriendlyByteBuf buffer) {
        entityID = buffer.readInt();
        return this;
    }

    @Override
    public void handle(ClientPacketListener handler, Minecraft client) {
        final Entity entity = client.player.level().getEntity(entityID);
        if (entity != null) {
            ClientEventHandler.checkEntity(entity);
        }
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}

