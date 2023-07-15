package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class LavaRescueGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:lavarescue");

    int inLavaTicks = 0;
    int counter     = 0;
    private Mob entity;

    public LavaRescueGoal(final Mob entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean canUse()
    {
        if (counter++ == 20)
        {
            counter = 0;

            if (entity.isInLava())
            {
                inLavaTicks++;
            }
            else
            {
                inLavaTicks = 0;
            }

            if (inLavaTicks > 20)
            {
                inLavaTicks = 0;
                BlockPos tpPos;
                if (entity.getTarget() != null)
                {
                    tpPos = entity.getTarget().blockPosition();
                }
                else
                {
                    tpPos = BossSpawnHandler.findSpawnPosForBoss((ServerLevel) entity.level, entity, entity.blockPosition());
                    if (tpPos == null)
                    {
                        final BossCapability cap = ((BossCapEntity) entity).getBossCap();
                        if (cap != null)
                        {
                            tpPos = BossSpawnHandler.findSpawnPosForBoss((ServerLevel) entity.level, entity, cap.getSpawnPos());
                        }
                    }
                }

                if (tpPos == null)
                {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                else
                {
                    entity.teleportTo(tpPos.getX(), tpPos.getY(), tpPos.getZ());
                }
            }
        }

        return false;
    }
}
