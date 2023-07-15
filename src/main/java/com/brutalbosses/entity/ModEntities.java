package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static EntityType<Entity> THROWN_ITEMC = Registry.register(Registry.ENTITY_TYPE, BrutalBosses.id("thrownitem"), FabricEntityTypeBuilder.create()
            .entityFactory(ThrownItemEntity::new)
            .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
            .trackedUpdateRate(2)
            .trackRangeBlocks(120)
            .forceTrackedVelocityUpdates(true)
            .spawnGroup(MobCategory.MISC)
            .build());

    public static void init() {


    }
}
