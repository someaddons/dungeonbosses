package com.brutalbosses.compat;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class Compat
{
    /**
     * Champions mod compat
     */
    private static IEntityCompat championsCompat = new IEntityCompat() {};

    /**
     * Compat managers, inject a new one for a callback
     */
    public static List<IEntityCompat> compatManagers = new ArrayList<>();

    private static List<IEntityCompat> disabled = new ArrayList<>();

    public static void applyAllCompats(
        final ServerLevelAccessor world,
        final BossType bossType,
        final BlockPos pos,
        final Entity boss)
    {
        for (final IEntityCompat compat : compatManagers)
        {
            if (disabled.contains(compat))
            {
                continue;
            }

            try
            {
                compat.applyCompatTo(world, bossType, pos, boss);
            }
            catch (Exception e)
            {
                BrutalBosses.LOGGER.warn("Failed to run compat for: " + bossType + " compat:" + compat + " disabling compat");
                disabled.add(compat);
            }
        }
    }

    public static void initCompat()
    {
        if (ModList.get().isLoaded("champions"))
        {
            championsCompat = new ChampionsCompat();
        }

        compatManagers.add(championsCompat);
    }
}
