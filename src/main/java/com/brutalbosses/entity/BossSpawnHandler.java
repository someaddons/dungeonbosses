package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.compat.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiPredicate;

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

            final Mob boss = bossType.createBossEntity(world.getLevel());

            if (boss == null)
            {
                return;
            }

            if (chest != null)
            {
                final ResourceLocation lootTable = chest.lootTable;
                BrutalBosses.LOGGER.debug(
                  "Spawning " + bossType.getID() + " at " + pos + " at " + chest.getDisplayName().getString() + " with:" + lootTable);
            }

            final BlockPos spawnPos = findSpawnPosForBoss(world, boss, pos);
            if (spawnPos == null)
            {
                boss.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            else
            {
                boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            }

            if (chest != null)
            {
                boss.getCapability(BOSS_CAP).orElse(null).setLootTable(chest.lootTable);
            }
            boss.getCapability(BOSS_CAP).orElse(null).setSpawnPos(pos);

            Compat.applyAllCompats(world, bossType, pos, boss);

            if (!boss.isRemoved())
            {
                world.addFreshEntity(boss);
            }
        }
        catch (Exception spawnException)
        {
            BrutalBosses.LOGGER.error("Boss: " + bossType.getID() + " failed to spawn! Error:", spawnException);
        }
    }

    public static BlockPos findSpawnPosForBoss(final BlockGetter world, final Entity boss, final BlockPos pos)
    {
        final BlockPos spawnPos = findAround(world, pos, 15, 10,
          (w, p) ->
          {
              if (!w.getBlockState(p.below()).isSolid())
              {
                  return false;
              }

              for (int x = Mth.floor((-boss.getBbWidth() + 1) / 2); x <= Mth.ceil((boss.getBbWidth() - 1) / 2); x++)
              {
                  for (int z = Mth.floor((-boss.getBbWidth() + 1) / 2); z <= Mth.ceil((boss.getBbWidth() - 1) / 2); z++)
                  {
                      for (int y = 0; y <= Mth.ceil(boss.getBbHeight()); y++)
                      {
                          if (!(w.getBlockState(p.offset(x, y, z)).isSolid()))
                          {
                              return false;
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
    public static void spawnRandomBoss(final ServerLevelAccessor world, final BlockPos pos)
    {
        final List<BossType> list = new ArrayList<>(BossTypeManager.instance.bosses.values());
        final BossType bossType = list.get(BrutalBosses.rand.nextInt(list.size()));
        spawnBoss(world, pos, bossType, null);
    }

    /**
     * Returns the first air position near the given start. Advances vertically first then horizontally
     *
     * @param start     start position
     * @param vRange    vertical search range
     * @param hRange    horizontal search range
     * @param predicate check predicate for the right block
     * @return position or null
     */
    public static BlockPos findAround(final BlockGetter world, final BlockPos start, final int vRange, final int hRange, final BiPredicate<BlockGetter, BlockPos> predicate)
    {
        if (vRange < 1 && hRange < 1)
        {
            return null;
        }

        BlockPos temp;
        int y = 0;
        int y_offset = 1;

        for (int i = 0; i < hRange + 2; i++)
        {
            for (int steps = 1; steps <= vRange; steps++)
            {
                // Start topleft of middle point
                temp = start.offset(-steps, y, -steps);

                // X ->
                for (int x = 0; x <= steps; x++)
                {
                    temp = temp.offset(1, 0, 0);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }

                // X
                // |
                // v
                for (int z = 0; z <= steps; z++)
                {
                    temp = temp.offset(0, 0, 1);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }

                // < - X
                for (int x = 0; x <= steps; x++)
                {
                    temp = temp.offset(-1, 0, 0);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }

                // ^
                // |
                // X
                for (int z = 0; z <= steps; z++)
                {
                    temp = temp.offset(0, 0, -1);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }
            }

            y += y_offset;
            y_offset = y_offset > 0 ? y_offset + 1 : y_offset - 1;
            y_offset *= -1;

            if (world.getHeight() <= start.getY() + y)
            {
                return null;
            }
        }

        return null;
    }
}
