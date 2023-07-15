package com.brutalbosses.entity.capability;

import org.jetbrains.annotations.Nullable;

public interface BossCapEntity {

    public void setBossCap(final BossCapability type);

    @Nullable
    public BossCapability getBossCap();
}
