package com.brutalbosses.compat;

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

    public static void applyAllCompats(
        final ServerLevelAccessor world,
        final BossType bossType,
        final BlockPos pos,
        final Entity boss)
    {
        for (final IEntityCompat compat : compatManagers)
        {
            compat.applyCompatTo(world, bossType, pos, boss);
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
