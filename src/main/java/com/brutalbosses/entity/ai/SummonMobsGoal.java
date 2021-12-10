package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
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
        if (target != null && target.isAlive() && params.entityIDs.size() > 0)
        {
            this.target = target;
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void start()
    {
        ticksToNextUpdate = Math.max(50, ticksToNextUpdate);
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
            if (ticksToNextUpdate < 30)
            {
                if (mob instanceof SpellcastingIllagerEntity)
                {
                    ((SpellcastingIllagerEntity) mob).setIsCastingSpell(SpellcastingIllagerEntity.SpellType.WOLOLO);
                }
            }

            return;
        }

        if (mob instanceof SpellcastingIllagerEntity)
        {
            ((SpellcastingIllagerEntity) mob).setIsCastingSpell(SpellcastingIllagerEntity.SpellType.NONE);
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

        final LivingEntity summoned = params.entityIDs.get(BrutalBosses.rand.nextInt(params.entityIDs.size())).create(mob.level);
        final BlockPos spawnPos = BossSpawnHandler.findSpawnPosForBoss((ServerWorld) mob.level, summoned, mob.blockPosition());

        if (spawnPos == null)
        {
            return;
        }
        ((ServerWorld) mob.level).sendParticles(ParticleTypes.CLOUD,
          spawnPos.getX(),
          spawnPos.getY() + 1,
          spawnPos.getZ(),
          20,
          0,
          0.0D,
          0,
          0.0D);

        if (summoned instanceof MobEntity)
        {
            ((MobEntity) summoned).setTarget(target);
            if (summoned instanceof IRangedAttackMob)
            {
                summoned.setItemInHand(Hand.MAIN_HAND, Items.BOW.getDefaultInstance());
            }
            else
            {
                summoned.setItemInHand(Hand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
            }
        }

        summoned.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        mob.level.addFreshEntity(summoned);
        summonedMobs.add(summoned);
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

        final List<EntityType<? extends LivingEntity>> types = new ArrayList<>();
        for (final JsonElement entityID : jsonElement.get(ENTITY_ID).getAsJsonArray())
        {
            types.add((EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityID.getAsString())));
        }

        params.entityIDs = types;
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
        private int                                      interval  = 500;
        private List<EntityType<? extends LivingEntity>> entityIDs = new ArrayList<>();
        private int                                      count     = 1;
        private int                                      maxcount  = 2;

        private SummonParams()
        {
        }
    }
}