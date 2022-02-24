package com.brutalbosses.event;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.entity.thrownentity.CSpriteRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ClientBossInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientEventHandler
{/*
    @SubscribeEvent
    public static void preRender(final RenderLivingEvent.Pre event)
    {
        if (event.getEntity() instanceof CustomEntityRenderData)
        {
            ((CustomEntityRenderData) event.getEntity()).setDimension(event.getEntity()
                                                                        .getType()
                                                                        .getDimensions()
                                                                        .scale(((CustomEntityRenderData) event.getEntity()).getVisualScale()));
        }
    }

    @SubscribeEvent
    public static void postRender(final RenderLivingEvent.Post event)
    {
        if (event.getEntity() instanceof CustomEntityRenderData)
        {
            ((CustomEntityRenderData) event.getEntity()).setDimension(event.getEntity()
                                                                        .getType()
                                                                        .getDimensions()
                                                                        .scale(((CustomEntityRenderData) event.getEntity()).getVisualScale()));
        }
    }*/

    /**
     * Map with entities and their boss bar UI
     */
    private static Map<Entity, ClientBossUI> bossInfoMap = new HashMap<>();
    static
    {
        // Not sure why this is needed
        Minecraft.getInstance().gui.getBossOverlay().events = new ConcurrentHashMap<>();
    }
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event)
    {
        // Clear any boss infos to avoid storing entities/Levels
        bossInfoMap.clear();
        Minecraft.getInstance().gui.getBossOverlay().events.clear();
    }

    @SubscribeEvent()
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (!event.player.level.isClientSide() || event.player.level.getGameTime() % 5 != 0)
        {
            return;
        }

        final Entity target = Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult instanceof EntityRayTraceResult
                                ? ((EntityRayTraceResult) Minecraft.getInstance().hitResult).getEntity()
                                : null;

        checkEntity(target);


        for (final Iterator<Map.Entry<Entity, ClientBossUI>> iterator = bossInfoMap.entrySet().iterator(); iterator.hasNext(); )
        {
            final Map.Entry<Entity, ClientBossUI> entry = iterator.next();
            entry.getValue().bossInfo.update(new SUpdateBossInfoPacketInfoHolder(entry.getKey(), entry.getValue().cap, SUpdateBossInfoPacket.Operation.UPDATE_PCT));
            if (!entry.getKey().isAlive() || entry.getValue().timeOut < entry.getKey().level.getGameTime()
                  || entry.getKey().blockPosition().distManhattan(event.player.blockPosition()) > 50)
            {
                Minecraft.getInstance().gui.getBossOverlay().events.remove(entry.getValue().bossInfo.getId());
                iterator.remove();
            }
            else
            {
                if (event.player.canSee(entry.getKey()))
                {
                    entry.getValue().timeOut = event.player.level.getGameTime() + 20 * 30;
                }
            }
        }
    }

    /**
     * Creates a new bossbar UI
     *
     * @param target
     * @param cap
     * @return
     */
    private static ClientBossUI createBossGUI(final Entity target, final BossCapability cap)
    {
        ClientBossUI ui = new ClientBossUI(new ClientBossInfo(new SUpdateBossInfoPacketInfoHolder(target, cap, SUpdateBossInfoPacket.Operation.ADD)), target, cap);
        Minecraft.getInstance().gui.getBossOverlay().events.put(ui.bossInfo.getId(), ui.bossInfo);
        return ui;
    }

    public static void checkEntity(final Entity target)
    {
        if (target instanceof LivingEntity)
        {
            final BossCapability cap = target.getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {
                if (bossInfoMap.containsKey(target))
                {
                    ClientBossUI ui = bossInfoMap.get(target);
                    ui.timeOut = target.level.getGameTime() + 20 * 30;
                }
                else
                {
                    bossInfoMap.put(target, createBossGUI(target, cap));
                }
            }
        }
    }

    private static class ClientBossUI
    {
        private ClientBossInfo bossInfo;
        private long           timeOut;
        private Entity         boss;
        private BossCapability cap;

        private ClientBossUI(final ClientBossInfo bossInfo, final Entity boss, final BossCapability cap)
        {
            this.bossInfo = bossInfo;
            this.timeOut = boss.level.getGameTime() + 20 * 30;
            this.boss = boss;
            this.cap = cap;
        }
    }

    public static void initRenderers()
    {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.THROWN_ITEMC,
          manager -> new CSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 1.0f, true));
    }
}
