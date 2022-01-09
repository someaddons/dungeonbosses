package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
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
import net.minecraft.scoreboard.ScorePlayerTeam;
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

    public SummonMobsGoal(MobEntity mob, final IAIParams params)
    {
        this.mob = mob;
        this.params = (SummonParams) params;
        ScorePlayerTeam team = mob.level.getScoreboard().getPlayerTeam("bb:bossteam");
        if (team == null)
        {
            team = mob.level.getScoreboard().addPlayerTeam("bb:bossteam");
        }
        mob.level.getScoreboard().addPlayerToTeam(mob.getScoreboardName(), team);
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && params.entityIDs.size() > 0)
        {
            this.target = target;
            return params.healthPhaseCheck.test(mob);
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

        summonedMobs.removeIf(summoned ->
        {
            if (!summoned.isAlive())
            {
                if (params.ownerdamage > 0)
                {
                    mob.setHealth(mob.getHealth() - mob.getMaxHealth() * params.ownerdamage);
                }
                return true;
            }
            return false;
        });

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
            ScorePlayerTeam team = mob.level.getScoreboard().getPlayerTeam("bb:bossteam");
            if (team == null)
            {
                team = mob.level.getScoreboard().addPlayerTeam("bb:bossteam");
            }
            mob.level.getScoreboard().addPlayerToTeam(summoned.getScoreboardName(), team);

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

    public static class SummonParams extends IAIParams.DefaultParams
    {
        private int                                      interval    = 500;
        private List<EntityType<? extends LivingEntity>> entityIDs   = new ArrayList<>();
        private int                                      count       = 1;
        private int                                      maxcount    = 2;
        private float                                    ownerdamage = 0f;

        public SummonParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String SUMM_INTERVAL = "interval";
        private static final String SUMM_MAX      = "maxcount";
        private static final String SUMM_COUNT    = "count";
        private static final String ENTITY_ID     = "entityid";
        private static final String OWNERDAMAGE   = "ownerdamagepct";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            final List<EntityType<? extends LivingEntity>> types = new ArrayList<>();
            for (final JsonElement entityID : jsonElement.get(ENTITY_ID).getAsJsonArray())
            {
                types.add((EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityID.getAsString())));
            }

            entityIDs = types;
            if (jsonElement.has(SUMM_INTERVAL))
            {
                interval = jsonElement.get(SUMM_INTERVAL).getAsInt();
            }

            if (jsonElement.has(SUMM_COUNT))
            {
                count = jsonElement.get(SUMM_COUNT).getAsInt();
            }

            if (jsonElement.has(SUMM_MAX))
            {
                maxcount = jsonElement.get(SUMM_MAX).getAsInt();
            }

            if (jsonElement.has(OWNERDAMAGE))
            {
                ownerdamage = jsonElement.get(OWNERDAMAGE).getAsFloat() / 100f;
            }

            return this;
        }
    }
}