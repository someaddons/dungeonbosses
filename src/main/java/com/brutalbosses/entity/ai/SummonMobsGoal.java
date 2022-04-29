package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.IEntityCapReader;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

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
        summonedMobs.removeIf(summoned ->
        {
            if (!summoned.isAlive())
            {
                if (params.ownerdamage > 0)
                {
                    mob.setHealth(Math.min(mob.getMaxHealth() * 0.1f, mob.getHealth() - mob.getMaxHealth() * params.ownerdamage));
                }
                return true;
            }
            return false;
        });

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

        final LivingEntity summoned;
        final EntityType entityType = params.entityIDs.get(BrutalBosses.rand.nextInt(params.entityIDs.size()));

        try
        {
            summoned = (LivingEntity) entityType.create(mob.level);
            if (params.entityNBTData.containsKey(entityType.getRegistryName()))
            {
                final CompoundNBT nbt = params.entityNBTData.get(entityType.getRegistryName());
                if (nbt.contains("Pos"))
                {
                    summoned.load(nbt);
                }
                else
                {
                    if (nbt.contains("ForgeCaps", 10) && summoned instanceof IEntityCapReader)
                    {
                        ((IEntityCapReader) summoned).readCapsFrom(nbt.getCompound("ForgeCaps"));
                    }
                    summoned.readAdditionalSaveData(nbt);
                }
                summoned.setUUID(UUID.randomUUID());
            }
            else
            {
                if (summoned instanceof IRangedAttackMob && summoned.getMainHandItem().isEmpty())
                {
                    summoned.setItemInHand(Hand.MAIN_HAND, Items.BOW.getDefaultInstance());
                }
            }
        }
        catch (Exception e)
        {
            final BossCapability bossCapability = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (bossCapability != null)
            {
                BrutalBosses.LOGGER.warn("Failed summoning add for boss:" + bossCapability.getBossType().getID(), e);
                return;
            }
            BrutalBosses.LOGGER.warn("Failed summoning addfor boss:", e);
            return;
        }

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
        }

        summoned.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        mob.level.addFreshEntity(summoned);
        summonedMobs.add(summoned);
    }

    public static class SummonParams extends IAIParams.DefaultParams
    {
        private int                                      interval      = 500;
        private List<EntityType<? extends LivingEntity>> entityIDs     = new ArrayList<>();
        private int                                      count         = 1;
        private int                                      maxcount      = 2;
        private float                                    ownerdamage   = 0f;
        private Map<ResourceLocation, CompoundNBT>       entityNBTData = new HashMap<>();

        public SummonParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String SUMM_INTERVAL   = "interval";
        private static final String SUMM_MAX        = "maxcount";
        private static final String SUMM_COUNT      = "count";
        private static final String ENTITY_ID       = "entityid";
        private static final String ENTITIES        = "entities";
        private static final String OWNERDAMAGE     = "ownerdamagepct";
        private static final String SUMM_ENTITY_NBT = "entitynbt";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(ENTITIES))
            {
                final Map<ResourceLocation, CompoundNBT> entityData = new HashMap<>();
                final List<EntityType<? extends LivingEntity>> types = new ArrayList<>();

                for (JsonElement entityEntry : jsonElement.get(ENTITIES).getAsJsonArray())
                {
                    final ResourceLocation entityID = new ResourceLocation(((JsonObject) entityEntry).get(ENTITY_ID).getAsString());
                    types.add((EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(entityID));
                    if (((JsonObject) entityEntry).has(SUMM_ENTITY_NBT))
                    {
                        try
                        {
                            entityData.put(entityID, JsonToNBT.parseTag(((JsonObject) entityEntry).get(SUMM_ENTITY_NBT).getAsString()));
                        }
                        catch (CommandSyntaxException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
                entityIDs = types;
                entityNBTData = entityData;
            }


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