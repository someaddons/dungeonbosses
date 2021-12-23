package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.world.PostStructureInfoGetter;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static com.brutalbosses.entity.capability.BossCapability.BOSS_CAP;

public class BossSpawnHandler
{
    /**
     * Handles boss spawn on chest placements
     *
     * @param world
     * @param chest
     * @param structure
     * @param mutableboundingbox
     */
    public static void onChestPlaced(final IServerWorld world, final LockableLootTileEntity chest)
    {
        List<BossType> possibleBosses = BossTypeManager.instance.lootTableSpawnEntries.get(chest.lootTable);
        if (possibleBosses != null && !possibleBosses.isEmpty())
        {
            if (BrutalBosses.rand.nextInt(100) < BrutalBosses.config.getCommonConfig().globalBossSpawnChance.get())
            {
                return;
            }

            final BossType bossType = possibleBosses.get(BrutalBosses.rand.nextInt(possibleBosses.size()));
            spawnBoss(world, chest.getBlockPos(), bossType, chest);
        }
        else if (BrutalBosses.config.getCommonConfig().printChestLoottable.get())
        {
            BrutalBosses.LOGGER.info("Chest with Loottable: " + chest.lootTable + " not associated with any boss spawn");
        }
    }

    /**
     * Spawns the boss at the given position
     *
     * @param world
     */
    public static void spawnBoss(final IServerWorld world, final BlockPos pos, final BossType bossType, @Nullable final LockableLootTileEntity chest)
    {
        final MobEntity boss = bossType.createBossEntity(world.getLevel());

        if (boss == null)
        {
            return;
        }

        if (chest != null)
        {
            final ResourceLocation lootTable = chest.lootTable;
            BrutalBosses.LOGGER.debug(
              "Spawning " + bossType.getID() + " at " + pos + " at " + chest.getDisplayName().getString() + " with:" + lootTable.toString() + " in "
                + ((PostStructureInfoGetter) world).getStructure()
                    .getFeatureName());
        }

        final BlockPos spawnPos = findSpawnPosForBoss(world, boss, pos);
        if (spawnPos == null)
        {
            boss.remove();
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

        world.addFreshEntity(boss);
    }

    public static BlockPos findSpawnPosForBoss(final IServerWorld world, final Entity boss, final BlockPos pos)
    {
        final BlockPos spawnPos = findAround(world, pos, 15, 10,
          (w, p) ->
          {
              final Material materialBelow = w.getBlockState(p.below()).getMaterial();
              if (!(materialBelow.isSolid() || materialBelow == Material.WATER))
              {
                  return false;
              }

              for (int x = MathHelper.floor((-boss.getBbWidth() + 1) / 2); x <= MathHelper.ceil((boss.getBbWidth() - 1) / 2); x++)
              {
                  for (int z = MathHelper.floor((-boss.getBbWidth() + 1) / 2); z <= MathHelper.ceil((boss.getBbWidth() - 1) / 2); z++)
                  {
                      for (int y = 0; y <= MathHelper.ceil(boss.getBbHeight()); y++)
                      {
                          final Material material = w.getBlockState(p.offset(x, y, z)).getMaterial();
                          if (!(material == Material.AIR || material == Material.WATER || material == Material.WATER_PLANT))
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
    public static void spawnRandomBoss(final IServerWorld world, final BlockPos pos)
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
    public static BlockPos findAround(final IServerWorld world, final BlockPos start, final int vRange, final int hRange, final BiPredicate<IBlockReader, BlockPos> predicate)
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
