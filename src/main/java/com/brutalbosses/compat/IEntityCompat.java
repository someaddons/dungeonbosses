package com.brutalbosses.compat;

import com.brutalbosses.entity.BossType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;

public interface IEntityCompat
{
    default void applyCompatTo(
      final ServerLevelAccessor world,
      final BossType bossType,
      final BlockPos pos,
      final Entity entity)
    {
        // No change by default
    }
}
