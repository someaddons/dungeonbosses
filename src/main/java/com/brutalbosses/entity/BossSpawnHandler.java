package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.compat.Compat;
import com.cupboard.util.BlockSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.brutalbosses.entity.capability.BossCapability.BOSS_CAP;

public class BossSpawnHandler
{
    private static ConcurrentLinkedQueue<Tuple<BlockPos, BossType>> spawns = new ConcurrentLinkedQueue<>();

    /**
     * Handles boss spawn on chest placements
     *
     * @param world
     * @param chest
     */
    public static void onChestPlaced(final ServerLevelAccessor world, final RandomizableContainerBlockEntity chest)
    {
        List<BossType> possibleBosses = BossTypeManager.instance.lootTableSpawnEntries.get(chest.lootTable);
        if (possibleBosses != null && !possibleBosses.isEmpty())
        {
            if (BrutalBosses.rand.nextInt(100) > BrutalBosses.config.getCommonConfig().globalBossSpawnChance)
            {
                return;
            }

            if (spawnedRecentlyClose(chest.getBlockPos()))
            {
                return;
            }

            BossType bossType = possibleBosses.get(BrutalBosses.rand.nextInt(possibleBosses.size()));

            if (bossType == null || bossType.getID().getPath().equals("dummyboss"))
            {
                return;
            }

            spawnBoss(world, chest.getBlockPos(), bossType, chest);
        }
        else if (BrutalBosses.config.getCommonConfig().printChestLoottable)
        {
            BrutalBosses.LOGGER.info("Chest with Loottable: " + chest.lootTable + " not associated with any boss spawn");
        }
    }

    /**
     * Check if we spawned the same bosstype closeby recently
     *
     * @param pos
     * @return
     */
    private static boolean spawnedRecentlyClose(final BlockPos pos)
    {
        for (final Tuple<BlockPos, BossType> data : spawns)
        {
            if (Math.sqrt(data.getA().distSqr(pos)) < BrutalBosses.config.getCommonConfig().minDistance)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Spawns the boss at the given position
     *
     * @param world
     */
    public static void spawnBoss(final ServerLevelAccessor world, final BlockPos pos, final BossType bossType, @Nullable final RandomizableContainerBlockEntity chest)
    {
        try
        {
            spawns.add(new Tuple<>(pos, bossType));
            if (spawns.size() > 20)
            {
                spawns.poll();
            }

            final CompoundTag bossTag = bossType.createBossTag(world.getLevel());
            if (bossTag == null)
            {
                return;
            }

            List<Entity> passengers = new ArrayList<>();
            // Load entity (and all passengers) from NBT
            Entity boss = EntityType.loadEntityRecursive(bossTag, world.getLevel(), e -> {
                e.setUUID(UUID.randomUUID());
                passengers.add(e);
                return e;
            });
            passengers.remove(boss);
            boss.setUUID(UUID.randomUUID());

            if (chest != null)
            {
                final ResourceLocation lootTable = chest.lootTable;
                BrutalBosses.LOGGER.debug(
                    "Spawning " + bossType.getID() + " at " + pos + " at " + chest.getDisplayName().getString() + " with:" + lootTable);
            }

            final BlockPos spawnPos = findSpawnPosForBoss(world, (LivingEntity) boss, pos);
            if (spawnPos == null)
            {
                boss.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            else
            {
                boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            }

            bossType.initForEntity((Mob) boss);
            ((Mob) boss).setHealth(((Mob) boss).getMaxHealth());
            if (boss instanceof AbstractVillager)
            {
                // Init empty offers to avoid offers creating maps during worldgen
                ((AbstractVillager) boss).offers = new MerchantOffers();
            }

            // Check loottable null to allow entity nbt setting it
            if (chest != null && boss.getCapability(BOSS_CAP).orElse(null).getLootTable() == null)
            {
                boss.getCapability(BOSS_CAP).orElse(null).setLootTable(chest.lootTable);
            }
            boss.getCapability(BOSS_CAP).orElse(null).setSpawnPos(spawnPos);

            Compat.applyAllCompats(world, bossType, pos, boss);

            if (!boss.isRemoved())
            {
                world.addFreshEntity(boss);
                for (final Entity passenger : passengers)
                {
                    passenger.getCapability(BOSS_CAP).ifPresent(cap -> cap.setSpawnPos(spawnPos));
                    passenger.setPos(boss.position());
                    if (passenger instanceof AbstractVillager)
                    {
                        // Init empty offers to avoid offers creating maps during worldgen
                        ((AbstractVillager) passenger).offers = new MerchantOffers();
                    }
                    world.addFreshEntity(passenger);
                }
            }
        }
        catch (Exception spawnException)
        {
            BrutalBosses.LOGGER.error("Boss: " + bossType.getID() + " failed to spawn! Error:", spawnException);
        }
    }

    public static BlockPos findSpawnPosForBoss(final ServerLevelAccessor world, final LivingEntity boss, final BlockPos pos)
    {
        final boolean allowWater = boss.canBreatheUnderwater();
        final BlockPos spawnPos = BlockSearch.findAround(world, pos, 15, 10, 1,
            (w, p) ->
            {
                if (w.getBlockState(p.below()).isAir())
                {
                    return false;
                }

                for (int x = Mth.floor((-boss.getBbWidth() + 1) / 2); x <= Mth.ceil((boss.getBbWidth() - 1) / 2); x++)
                {
                    for (int z = Mth.floor((-boss.getBbWidth() + 1) / 2); z <= Mth.ceil((boss.getBbWidth() - 1) / 2); z++)
                    {
                        for (int y = 0; y <= Mth.ceil(boss.getBbHeight()); y++)
                        {
                            final BlockState state = w.getBlockState(p.offset(x, y, z));
                            if (!(state.isAir()))
                            {
                                if ((!allowWater || !w.getFluidState(p.offset(x, y, z)).is(FluidTags.WATER)))
                                {
                                    return false;
                                }
                            }
                        }
                    }
                }

                return true;
            });

        return spawnPos;
    }

    /**
     * Spawns a random boss at the given pos
     *
     * @param world
     * @param pos
     */
    public static void spawnRandomBoss(final ServerLevel world, final BlockPos pos)
    {
        final List<BossType> list = new ArrayList<>(BossTypeManager.instance.bosses.values());
        final BossType bossType = list.get(BrutalBosses.rand.nextInt(list.size()));
        spawnBoss(world, pos, bossType, null);
    }
}
