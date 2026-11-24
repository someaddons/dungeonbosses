package com.brutalbosses.compat;

import com.brutalbosses.entity.BossType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;

import java.util.ArrayList;
import java.util.List;

public class ChampionsCompat implements IEntityCompat
{
    public void applyCompatTo(
        final ServerLevelAccessor world,
        final BossType bossType,
        final BlockPos pos,
        final Entity entity)
    {
        if (entity instanceof LivingEntity livingEntity)
        {
            ChampionsApi.get().getChampion(livingEntity).ifPresent(
                champion -> {
                    if (champion instanceof Champion.Server serverChampion)
                    {
                        final List<AffixInstance> affixes = new ArrayList<>(serverChampion.affixes());

                        for (final AffixInstance instance : affixes)
                        {
                            serverChampion.removeAffix(instance);
                        }
                    }
                }
            );
        }
    }
}
