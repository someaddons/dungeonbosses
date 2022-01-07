package com.brutalbosses.entity;

import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ModEntities
{
    public static EntityType<ThrownItemEntity> THROWN_ITEMC = build(ThrownItemEntity.ID,
      EntityType.Builder.of((EntityType<ThrownItemEntity> type, World world) -> new ThrownItemEntity(type, world), EntityClassification.MISC)
        .setTrackingRange(120)
        .setUpdateInterval(2)
        .sized(0.5F, 0.5F)
        .setShouldReceiveVelocityUpdates(true));

    private static <T extends Entity> EntityType<T> build(final ResourceLocation id, final EntityType.Builder<T> builder)
    {
        final EntityType<T> entityType = builder.build(id.toString());
        entityType.setRegistryName(id);
        return entityType;
    }
}
