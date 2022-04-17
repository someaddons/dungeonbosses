package com.brutalbosses.world;

import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public interface PostStructureInfoGetter
{
    public ConfiguredStructureFeature<?, ?> getStructure();

    public void setCurrent(final ConfiguredStructureFeature<?, ?> current);
}
