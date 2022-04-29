package com.brutalbosses.entity;

import net.minecraft.nbt.CompoundNBT;

public interface IEntityCapReader
{
    public void readCapsFrom(final CompoundNBT tag);
}
