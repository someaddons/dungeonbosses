package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Simply chases the target at the required distance
 */
public class SummonMobsGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:summonmobs");

    private final Mob          mob;
    private       LivingEntity target = null;
    private final SummonParams params;

    private final List<LivingEntity> summonedMobs = new ArrayList<>();

    public SummonMobsGoal(Mob mob, final IAIParams params)
    {
        this.mob = mob;
        this.params = (SummonParams) params;
        PlayerTeam team = mob.level.getScoreboard().getPlayerTeam("bb:bossteam");
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
                if (mob instanceof SpellcasterIllager)
                {
                    ((SpellcasterIllager) mob).setIsCastingSpell(SpellcasterIllager.IllagerSpell.WOLOLO);
                }
            }

            return;
        }

        if (mob instanceof SpellcasterIllager)
        {
            ((SpellcasterIllager) mob).setIsCastingSpell(SpellcasterIllager.IllagerSpell.NONE);
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

        final LivingEntity summoned;
        final EntityType entityType = params.entityIDs.get(BrutalBosses.rand.nextInt(params.entityIDs.size()));

        try
        {
            summoned = (LivingEntity) entityType.create(mob.level);
            if (params.entityNBTData.containsKey(entityType.getRegistryName()))
            {
                if (params.entityNBTData.get(entityType.getRegistryName()).contains("Pos"))
                {
                    summoned.load(params.entityNBTData.get(entityType.getRegistryName()));
                }
                else
                {
                    summoned.readAdditionalSaveData(params.entityNBTData.get(entityType.getRegistryName()));
                }
                summoned.setUUID(UUID.randomUUID());
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

        final BlockPos spawnPos = BossSpawnHandler.findSpawnPosForBoss((ServerLevel) mob.level, summoned, mob.blockPosition());

        if (spawnPos == null)
        {
            return;
        }
        ((ServerLevel) mob.level).sendParticles(ParticleTypes.CLOUD,
          spawnPos.getX(),
          spawnPos.getY() + 1,
          spawnPos.getZ(),
          20,
          0,
          0.0D,
          0,
          0.0D);

        if (summoned instanceof Mob)
        {
            PlayerTeam team = mob.level.getScoreboard().getPlayerTeam("bb:bossteam");
            if (team == null)
            {
                team = mob.level.getScoreboard().addPlayerTeam("bb:bossteam");
            }
            mob.level.getScoreboard().addPlayerToTeam(summoned.getScoreboardName(), team);

            ((Mob) summoned).setTarget(target);
            if (summoned instanceof RangedAttackMob)
            {
                summoned.setItemInHand(InteractionHand.MAIN_HAND, Items.BOW.getDefaultInstance());
            }
            else
            {
                summoned.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
            }
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
        private Map<ResourceLocation, CompoundTag>       entityNBTData = new HashMap<>();

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
                final Map<ResourceLocation, CompoundTag> entityData = new HashMap<>();
                final List<EntityType<? extends LivingEntity>> types = new ArrayList<>();

                for (JsonElement entityEntry : jsonElement.get(ENTITIES).getAsJsonArray())
                {
                    final ResourceLocation entityID = new ResourceLocation(((JsonObject) entityEntry).get(ENTITY_ID).getAsString());
                    types.add((EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(entityID));
                    if (((JsonObject) entityEntry).has(SUMM_ENTITY_NBT))
                    {
                        try
                        {
                            entityData.put(entityID, TagParser.parseTag(((JsonObject) entityEntry).get(SUMM_ENTITY_NBT).getAsString()));
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