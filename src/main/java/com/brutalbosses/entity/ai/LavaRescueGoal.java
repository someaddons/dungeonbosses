package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class LavaRescueGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:lavarescue");

    int inLavaTicks = 0;
    int counter     = 0;
    private MobEntity entity;

    public LavaRescueGoal(final MobEntity entity)
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
                    tpPos = BossSpawnHandler.findSpawnPosForBoss((ServerWorld) entity.level, entity, entity.blockPosition());
                    if (tpPos == null)
                    {
                        final BossCapability cap = entity.getCapability(BossCapability.BOSS_CAP).orElse(null);
                        if (cap != null)
                        {
                            tpPos = BossSpawnHandler.findSpawnPosForBoss((ServerWorld) entity.level, entity, cap.getSpawnPos());
                        }
                    }
                }

                if (tpPos == null)
                {
                    entity.remove();
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
