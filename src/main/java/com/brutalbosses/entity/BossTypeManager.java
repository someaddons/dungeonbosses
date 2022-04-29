package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ai.*;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Manages and holds all boss entries and stores the capability
 */
public class BossTypeManager
{
    public              Map<ResourceLocation, BossType>                        bosses                = ImmutableMap.of();
    public              Set<ResourceLocation>                                  entityTypes           = ImmutableSet.of();
    public static final BossTypeManager                                        instance              = new BossTypeManager();
    public              Map<ResourceLocation, BiConsumer<Entity, IAIParams>>   aiCreatorRegistry     = ImmutableMap.of();
    public              Map<ResourceLocation, Function<JsonObject, IAIParams>> aiParamParsers        = ImmutableMap.of();
    public              ImmutableMap<ResourceLocation, List<BossType>>         lootTableSpawnEntries = ImmutableMap.of();

    private BossTypeManager()
    {


        registerAI(new ResourceLocation("minecraft:randomwalk"),
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new RandomWalkingGoal((CreatureEntity) entity, 0.8d, 20)),
          null);

        registerAI(new ResourceLocation("minecraft:meleeattack"),
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new MeleeAttackGoal((CreatureEntity) entity, 1.0d, true)),
          null);

        registerAI(new ResourceLocation("minecraft:crossbow"),
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000,
            new RangedCrossbowAttackGoal<>((MonsterEntity & IRangedAttackMob & ICrossbowUser) entity, 1.0d, 30)),
          null);

        registerAI(MeleeShieldAttackGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2001, new MeleeShieldAttackGoal((MobEntity) entity, 1.0d)),
          null);

        registerAI(new ResourceLocation("minecraft:target"),
          (entity, params) -> ((MobEntity) entity).targetSelector.addGoal(-2000, new NearestAttackableTargetGoal<>((MobEntity) entity, PlayerEntity.class, true)),
          null);

        registerAI(LavaRescueGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new LavaRescueGoal((MobEntity) entity)),
          null);

        registerAI(ChasingGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2001, new ChasingGoal((MobEntity) entity, params)),
          ChasingGoal.ChaseParams::new);

        registerAI(SmallFireballAttackGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SmallFireballAttackGoal((MobEntity) entity, params)),
          SimpleRangedAttackGoal.RangedParams::new);

        registerAI(WitherSkullAttackGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new WitherSkullAttackGoal((MobEntity) entity, params)),
          WitherSkullAttackGoal.WitherSkullParams::new);

        registerAI(SnowballAttackGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SnowballAttackGoal((MobEntity) entity, params)),
          SimpleRangedAttackGoal.RangedParams::new);

        registerAI(OutofCombatRegen.ID, (entity, params) -> ((MobEntity) entity).targetSelector.addGoal(-2000, new OutofCombatRegen((MobEntity) entity, params)),
          OutofCombatRegen.CombatParams::new);

        registerAI(SpitCobwebGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SpitCobwebGoal((MobEntity) entity, params)),
          SimpleRangedAttackGoal.RangedParams::new);

        registerAI(SummonMobsGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SummonMobsGoal((MobEntity) entity, params)),
          SummonMobsGoal.SummonParams::new);

        registerAI(WhirlWindMelee.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new WhirlWindMelee((MobEntity) entity, params)),
          WhirlWindMelee.WhirldWindParams::new);

        registerAI(new ResourceLocation("brutalbosses:whirldwind"),
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new WhirlWindMelee((MobEntity) entity, params)),
          WhirlWindMelee.WhirldWindParams::new);

        registerAI(MeleeHitGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new MeleeHitGoal((MobEntity) entity, params)),
          MeleeHitGoal.MeleeHitParams::new);

        registerAI(ChargeGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new ChargeGoal((MobEntity) entity, params)),
          ChargeGoal.ChargeParams::new);

        registerAI(BigFireballAttackGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new BigFireballAttackGoal((MobEntity) entity, params)),
          BigFireballAttackGoal.RangedParams::new);

        registerAI(ItemThrowAttackGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new ItemThrowAttackGoal((MobEntity) entity, params)),
          ItemThrowAttackGoal.ItemThrowParams::new);

        registerAI(TemporaryPotionGoal.ID,
          (entity, params) -> ((MobEntity) entity).goalSelector.addGoal(-2000, new TemporaryPotionGoal((MobEntity) entity, params)),
          TemporaryPotionGoal.TempPotionParams::new);
    }

    /**
     * Register the AI and adds additional alternative ID ones ending with 1 - 4
     *
     * @param ID
     * @param aiCreator
     * @param paramsParser
     */
    public void registerAI(ResourceLocation ID, BiConsumer<Entity, IAIParams> aiCreator, @Nullable Function<JsonObject, IAIParams> paramsParser)
    {
        final ImmutableMap.Builder<ResourceLocation, BiConsumer<Entity, IAIParams>> aiRegistry = ImmutableMap.builder();
        final ImmutableMap.Builder<ResourceLocation, Function<JsonObject, IAIParams>> aiSupplier = ImmutableMap.<ResourceLocation, Function<JsonObject, IAIParams>>builder();

        aiRegistry.putAll(this.aiCreatorRegistry);
        aiSupplier.putAll(this.aiParamParsers);

        aiRegistry.put(ID, aiCreator);
        if (paramsParser != null)
        {
            aiSupplier.put(ID, paramsParser);
        }

        for (int i = 1; i < 5; i++)
        {
            final ResourceLocation additionalID = new ResourceLocation(ID.getNamespace(), ID.getPath() + i);
            aiRegistry.put(additionalID, aiCreator);
            if (paramsParser != null)
            {
                aiSupplier.put(additionalID, paramsParser);
            }
        }

        this.aiCreatorRegistry = aiRegistry.build();
        this.aiParamParsers = aiSupplier.build();
    }

    /**
     * After reloading data we recalc some stuff
     */
    public void afterLoad()
    {
        final ImmutableSet.Builder<ResourceLocation> entityTypes = ImmutableSet.<ResourceLocation>builder();
        final HashMap<ResourceLocation, List<BossType>> tempSpawns = new HashMap<>();

        for (final BossType bossType : bosses.values())
        {
            entityTypes.add(bossType.getEntityType().getRegistryName());
            BrutalBosses.LOGGER.info("Loaded boss variant for: " + bossType.getEntityType().getRegistryName());

            for (final Map.Entry<ResourceLocation, Integer> spawnEntry : bossType.getSpawnTables().entrySet())
            {
                final List<BossType> contained = tempSpawns.computeIfAbsent(spawnEntry.getKey(), loc -> new ArrayList<>());
                for (int i = 0; i < spawnEntry.getValue(); i++)
                {
                    contained.add(bossType);
                }
            }
        }

        this.entityTypes = entityTypes.build();

        final ImmutableMap.Builder<ResourceLocation, List<BossType>> spawnMap = ImmutableMap.<ResourceLocation, List<BossType>>builder();
        for (final Map.Entry<ResourceLocation, List<BossType>> entry : tempSpawns.entrySet())
        {
            final ImmutableList.Builder<BossType> bossList = ImmutableList.<BossType>builder();
            bossList.addAll(entry.getValue());
            spawnMap.put(entry.getKey(), bossList.build());
        }

        this.lootTableSpawnEntries = spawnMap.build();
    }

    /**
     * Registers the capability manager
     */
    public void register()
    {
        CapabilityManager.INSTANCE.register(BossCapability.class, new Capability.IStorage<BossCapability>()
        {
            @Nullable
            @Override
            public INBT writeNBT(final Capability<BossCapability> capability, final BossCapability instance, final Direction side)
            {
                return capability.writeNBT(instance, side);
            }

            @Override
            public void readNBT(final Capability<BossCapability> capability, final BossCapability instance, final Direction side, final INBT nbt)
            {
                capability.readNBT(instance, side, nbt);
            }
        }, BossCapability::new);
    }

    /**
     * Check if we have a valid entity type for bosses
     *
     * @param entity
     * @return
     */
    public boolean isValidBossEntity(final Entity entity)
    {
        return entityTypes.contains(entity.getType().getRegistryName());
    }

    /**
     * Sets the boss types after load
     *
     * @param bossTypes
     */
    public void setBossTypes(final ImmutableMap<ResourceLocation, BossType> bossTypes)
    {
        this.bosses = bossTypes;
    }
}
