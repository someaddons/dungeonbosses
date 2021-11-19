package com.brutalbosses.event;

import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * Fake packet to pass along information for the ClientBossInfo
 */
public class SUpdateBossInfoPacketInfoHolder extends SUpdateBossInfoPacket
{
    private final UUID                            id = UUID.randomUUID();
    private final LivingEntity                    target;
    private final BossCapability                  capability;
    private final SUpdateBossInfoPacket.Operation operation;

    public SUpdateBossInfoPacketInfoHolder(
      final Entity target,
      final BossCapability capability, final SUpdateBossInfoPacket.Operation operation)
    {
        this.target = (LivingEntity) target;
        this.capability = capability;
        this.operation = operation;
    }

    @OnlyIn(Dist.CLIENT)
    public UUID getId()
    {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public SUpdateBossInfoPacket.Operation getOperation()
    {
        return operation;
    }

    @OnlyIn(Dist.CLIENT)
    public ITextComponent getName()
    {
        return target.getDisplayName();
    }

    @OnlyIn(Dist.CLIENT)
    public float getPercent()
    {
        return target.getHealth() / target.getMaxHealth();
    }

    @OnlyIn(Dist.CLIENT)
    public BossInfo.Color getColor()
    {
        return BossInfo.Color.BLUE;
    }

    @OnlyIn(Dist.CLIENT)
    public BossInfo.Overlay getOverlay()
    {
        return BossInfo.Overlay.PROGRESS;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldDarkenScreen()
    {
        return Minecraft.getInstance().player.blockPosition().distManhattan(target.blockPosition()) < 10;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldPlayMusic()
    {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldCreateWorldFog()
    {
        return false;
    }
}
