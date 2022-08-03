package com.brutalbosses.event;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class ModEventHandler
{
    @SubscribeEvent
    public static void onConfigChanged(ModConfigEvent event)
    {

    }

    @SubscribeEvent
    public static void registerEntities(final RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ENTITY_TYPES))
        {
            event.getForgeRegistry().register(ThrownItemEntity.ID, ModEntities.THROWN_ITEMC);
        }
    }
}
