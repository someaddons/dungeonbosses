package com.brutalbosses.compat;

import com.brutalbosses.entity.BossType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionCapability;
import top.theillusivec4.champions.common.rank.RankManager;

public class ChampionsCompat implements IEntityCompat
{
    public void applyCompatTo(
      final ServerLevelAccessor world,
      final BossType bossType,
      final BlockPos pos,
      final Entity entity)
    {
        ChampionCapability.getCapability((LivingEntity) entity).ifPresent(
          champion -> {
              IChampion.Server serverChampion = champion.getServer();
              serverChampion.setRank(RankManager.getLowestRank());
          }
        );
    }
}
