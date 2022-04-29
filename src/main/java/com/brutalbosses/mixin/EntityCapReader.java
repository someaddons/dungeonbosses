package com.brutalbosses.mixin;

import com.brutalbosses.entity.IEntityCapReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityCapReader extends net.minecraftforge.common.capabilities.CapabilityProvider<Entity> implements IEntityCapReader
{
    protected EntityCapReader(final Class<Entity> baseClass)
    {
        super(baseClass);
    }

    @Override
    public void readCapsFrom(final CompoundTag tag)
    {
        deserializeCaps(tag);
    }
}
