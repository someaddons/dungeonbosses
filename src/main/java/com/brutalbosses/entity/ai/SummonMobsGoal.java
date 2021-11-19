package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Simply chases the target at the required distance
 */
public class SummonMobsGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:summonmobs");

    private final MobEntity    mob;
    private       LivingEntity target = null;
    private final SummonParams params;

    private final List<LivingEntity> summonedMobs = new ArrayList<>();

    public SummonMobsGoal(MobEntity mob)
    {
        this.mob = mob;
        params = (SummonParams) mob.getCapability(BossCapability.BOSS_CAP).orElse(null).getBossType().getAIParams(ID);
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && params.entityID != null)
        {
            this.target = target;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void stop()
    {
        this.target = null;
    }

    private int ticksToNextUpdate = 0;

    public void tick()
    {
        if (--ticksToNextUpdate > 0)
        {
            return;
        }

        ticksToNextUpdate = params.interval;

        summonedMobs.removeIf(summoned -> !summoned.isAlive());

        for (int i = 0; i < params.count; i++)
        {
            trySummonMob();
        }
    }

    private void trySummonMob()
    {
        if (summonedMobs.size() >= params.maxcount)
        {
            return;
        }

        final LivingEntity summoned = params.entityID.create(mob.level);
        final BlockPos spawnPos = BossSpawnHandler.findSpawnPosForBoss((ServerWorld) mob.level, summoned, mob.blockPosition());

        if (spawnPos == null)
        {
            return;
        }

        summoned.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        mob.level.addFreshEntity(summoned);
    }

    public static final String SUMM_INTERVAL = "interval";
    public static final String SUMM_MAX      = "maxcount";
    public static final String SUMM_COUNT    = "count";
    public static final String ENTITY_ID     = "entityid";

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    public static IAIParams parse(final JsonObject jsonElement)
    {
        final SummonParams params = new SummonParams();

        params.entityID = (EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(new ResourceLocation(jsonElement.get(ENTITY_ID).getAsString()));

        if (jsonElement.has(SUMM_INTERVAL))
        {
            params.interval = jsonElement.get(SUMM_INTERVAL).getAsInt();
        }

        if (jsonElement.has(SUMM_COUNT))
        {
            params.count = jsonElement.get(SUMM_COUNT).getAsInt();
        }

        if (jsonElement.has(SUMM_MAX))
        {
            params.maxcount = jsonElement.get(SUMM_MAX).getAsInt();
        }


        return params;
    }

    private static class SummonParams implements IAIParams
    {
        private int                                interval = 500;
        private EntityType<? extends LivingEntity> entityID = EntityType.ZOMBIE;
        private int                                count    = 1;
        private int                                maxcount = 2;

        private SummonParams()
        {
        }
    }
}