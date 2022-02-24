package com.brutalbosses.world;

import net.minecraft.world.level.levelgen.feature.StructureFeature;

public interface PostStructureInfoGetter
{
    public StructureFeature<?> getStructure();

    public void setCurrent(final StructureFeature<?> current);
}
