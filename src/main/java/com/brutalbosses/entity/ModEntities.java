package com.brutalbosses.entity;

import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

public class ModEntities
{
    public static EntityType<ThrownItemEntity> THROWN_ITEMC = build(ThrownItemEntity.ID,
      EntityType.Builder.of((EntityType<ThrownItemEntity> type, Level world) -> new ThrownItemEntity(type, world), MobCategory.MISC)
        .setTrackingRange(120)
        .setUpdateInterval(2)
        .sized(0.5F, 0.5F)
        .setShouldReceiveVelocityUpdates(true));

    private static <T extends Entity> EntityType<T> build(final ResourceLocation id, final EntityType.Builder<T> builder)
    {
        return builder.build(id.toString());
    }
}
