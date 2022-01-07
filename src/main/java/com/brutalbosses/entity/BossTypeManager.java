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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages and holds all boss entries and stores the capability
 */
public class BossTypeManager
{
    public              Map<ResourceLocation, BossType>                        bosses                = ImmutableMap.of();
    public              Set<ResourceLocation>                                  entityTypes           = ImmutableSet.of();
    public static final BossTypeManager                                        instance              = new BossTypeManager();
    public              Map<ResourceLocation, Consumer<Entity>>                aiRegistry            = ImmutableMap.of();
    public              Map<ResourceLocation, Function<JsonObject, IAIParams>> aiSuppliers           = ImmutableMap.of();
    public              ImmutableMap<ResourceLocation, List<BossType>>         lootTableSpawnEntries = ImmutableMap.of();

    private BossTypeManager()
    {
        final ImmutableMap.Builder<ResourceLocation, Consumer<Entity>> aiRegistry = ImmutableMap.<ResourceLocation, Consumer<Entity>>builder();
        final ImmutableMap.Builder<ResourceLocation, Function<JsonObject, IAIParams>> aiSupplier = ImmutableMap.<ResourceLocation, Function<JsonObject, IAIParams>>builder();

        aiRegistry.put(new ResourceLocation("minecraft:randomwalk"),
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new RandomWalkingGoal((CreatureEntity) entity, 0.8d, 20)));

        aiRegistry.put(new ResourceLocation("minecraft:meleeattack"),
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new MeleeAttackGoal((CreatureEntity) entity, 1.0d, true)));

        aiRegistry.put(new ResourceLocation("minecraft:crossbow"),
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new RangedCrossbowAttackGoal<>((MonsterEntity & IRangedAttackMob & ICrossbowUser) entity, 1.0d, 30)));

        aiRegistry.put(MeleeShieldAttackGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2001, new MeleeShieldAttackGoal((MobEntity) entity, 1.0d)));

        aiRegistry.put(new ResourceLocation("minecraft:target"),
          entity -> ((MobEntity) entity).targetSelector.addGoal(-2000, new NearestAttackableTargetGoal<>((MobEntity) entity, PlayerEntity.class, true)));

        aiRegistry.put(LavaRescueGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new LavaRescueGoal((MobEntity) entity)));

        aiRegistry.put(ChasingGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2001, new ChasingGoal((MobEntity) entity)));
        aiSupplier.put(ChasingGoal.ID, ChasingGoal.ChaseParams::new);

        aiRegistry.put(SmallFireballAttackGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SmallFireballAttackGoal((MobEntity) entity)));
        aiSupplier.put(SmallFireballAttackGoal.ID, SimpleRangedAttackGoal.RangedParams::new);

        aiRegistry.put(WitherSkullAttackGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new WitherSkullAttackGoal((MobEntity) entity)));
        aiSupplier.put(WitherSkullAttackGoal.ID, WitherSkullAttackGoal.WitherSkullParams::new);

        aiRegistry.put(SnowballAttackGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SnowballAttackGoal((MobEntity) entity)));
        aiSupplier.put(SnowballAttackGoal.ID, SimpleRangedAttackGoal.RangedParams::new);

        aiRegistry.put(OutofCombatRegen.ID, entity -> ((MobEntity) entity).targetSelector.addGoal(-2000, new OutofCombatRegen((MobEntity) entity)));
        aiSupplier.put(OutofCombatRegen.ID, OutofCombatRegen.CombatParams::new);

        aiRegistry.put(SpitCobwebGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SpitCobwebGoal((MobEntity) entity)));
        aiSupplier.put(SpitCobwebGoal.ID, SimpleRangedAttackGoal.RangedParams::new);

        aiRegistry.put(SummonMobsGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new SummonMobsGoal((MobEntity) entity)));
        aiSupplier.put(SummonMobsGoal.ID, SummonMobsGoal.SummonParams::new);

        aiRegistry.put(WhirldwindMelee.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new WhirldwindMelee((MobEntity) entity)));
        aiSupplier.put(WhirldwindMelee.ID, WhirldwindMelee.WhirldWindParams::new);

        aiRegistry.put(MeleeHitGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new MeleeHitGoal((MobEntity) entity)));
        aiSupplier.put(MeleeHitGoal.ID, MeleeHitGoal.MeleeHitParams::new);

        aiRegistry.put(ChargeGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new ChargeGoal((MobEntity) entity)));
        aiSupplier.put(ChargeGoal.ID, ChargeGoal.ChargeParams::new);

        aiRegistry.put(BigFireballAttackGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new BigFireballAttackGoal((MobEntity) entity)));
        aiSupplier.put(BigFireballAttackGoal.ID, BigFireballAttackGoal.RangedParams::new);

        aiRegistry.put(ItemThrowAttackGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new ItemThrowAttackGoal((MobEntity) entity)));
        aiSupplier.put(ItemThrowAttackGoal.ID, ItemThrowAttackGoal.ItemThrowParams::new);

        aiRegistry.put(TemporaryPotionGoal.ID,
          entity -> ((MobEntity) entity).goalSelector.addGoal(-2000, new TemporaryPotionGoal((MobEntity) entity)));
        aiSupplier.put(TemporaryPotionGoal.ID, TemporaryPotionGoal.TempPotionParams::new);

        this.aiRegistry = aiRegistry.build();
        this.aiSuppliers = aiSupplier.build();
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
