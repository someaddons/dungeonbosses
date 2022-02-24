package com.brutalbosses.event;

import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class ClientBossEvent extends LerpingBossEvent
{
    public ClientBossEvent(final LivingEntity target, final BossCapability cap)
    {
        super(UUID.randomUUID(), target.getDisplayName(), target.getHealth() / target.getMaxHealth(), BossBarColor.BLUE, BossBarOverlay.PROGRESS, true, true, false);
    }

    public void update(final LivingEntity entity, final BossCapability cap)
    {
        setProgress(entity.getHealth() / entity.getMaxHealth());
    }
}
