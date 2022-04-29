package com.brutalbosses.mixin;

import com.brutalbosses.entity.IEntityCapReader;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityCapReader extends net.minecraftforge.common.capabilities.CapabilityProvider<Entity> implements IEntityCapReader
{
    protected EntityCapReader(final Class<Entity> baseClass)
    {
        super(baseClass);
    }

    @Override
    public void readCapsFrom(final CompoundNBT tag)
    {
        deserializeCaps(tag);
    }
}
