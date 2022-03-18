package com.brutalbosses.compat;

import com.brutalbosses.entity.BossType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.fml.ModList;

public class Compat
{
    /**
     * Champions mod compat
     */
    private static IEntityCompat championsCompat = new IEntityCompat() {};

    public static void applyAllCompats(
      final ServerLevelAccessor world,
      final BossType bossType,
      final BlockPos pos,
      final Entity boss)
    {
        championsCompat.applyCompatTo(world, bossType, pos, boss);
    }

    public static void initCompat()
    {
        if (ModList.get().isLoaded("champions"))
        {
            championsCompat = new ChampionsCompat();
        }
    }
}
