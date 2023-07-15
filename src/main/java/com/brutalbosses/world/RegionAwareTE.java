package com.brutalbosses.world;

import net.minecraft.world.level.ServerLevelAccessor;

public interface RegionAwareTE
{
    public void setRegion(final ServerLevelAccessor region);
}
