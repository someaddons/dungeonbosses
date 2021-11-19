package com.brutalbosses.world;

import net.minecraft.world.gen.feature.structure.Structure;

public interface PostStructureInfoGetter
{
    public Structure<?> getStructure();

    public void setCurrent(final Structure<?> current);
}
