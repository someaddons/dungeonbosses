package com.brutalbosses.event;

import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClientEventHandler {
    /*
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

    static {
        // Not sure why this is needed
        //Minecraft.getInstance().gui.getBossOverlay().events = new ConcurrentHashMap<>();
    }

    public static void onWorldUnload() {
        // Clear any boss infos to avoid storing entities/Levels
        bossInfoMap.clear();
        Minecraft.getInstance().gui.getBossOverlay().events.clear();
    }

    public static void onPlayerTick(LocalPlayer player) {
        if (!player.level().isClientSide() || player.level().getGameTime() % 5 != 0)
        {
            return;
        }

        final Entity target = Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult instanceof EntityHitResult
                ? ((EntityHitResult) Minecraft.getInstance().hitResult).getEntity()
                : null;

        checkEntity(target);


        for (final Iterator<Map.Entry<Entity, ClientBossUI>> iterator = bossInfoMap.entrySet().iterator(); iterator.hasNext(); ) {
            final Map.Entry<Entity, ClientBossUI> entry = iterator.next();
            entry.getValue().bossInfo.update((LivingEntity) entry.getKey(), entry.getValue().cap);
            if (!entry.getKey().isAlive() || entry.getValue().timeOut < entry.getKey().level().getGameTime())
            {
                Minecraft.getInstance().gui.getBossOverlay().events.remove(entry.getValue().bossInfo.getId());
                iterator.remove();
            }
            else
            {
                if (player.hasLineOfSight(entry.getKey()))
                {
                    entry.getValue().timeOut = player.level().getGameTime() + 20 * 30;
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
    private static ClientBossUI createBossGUI(final LivingEntity target, final BossCapability cap) {
        ClientBossUI ui = new ClientBossUI(new ClientBossEvent(target, cap), target, cap);
        Minecraft.getInstance().gui.getBossOverlay().events.put(ui.bossInfo.getId(), ui.bossInfo);
        return ui;
    }

    public static void checkEntity(final Entity target) {
        if (target instanceof BossCapEntity) {
            final BossCapability cap = ((BossCapEntity) target).getBossCap();
            if (cap != null && cap.isBoss() && cap.getBossType().showBossBar()) {
                if (bossInfoMap.containsKey(target)) {
                    ClientBossUI ui = bossInfoMap.get(target);
                    ui.timeOut = target.level().getGameTime() + 20 * 30;
                } else {
                    bossInfoMap.put(target, createBossGUI((LivingEntity) target, cap));
                }
            }
        }
    }

    private static class ClientBossUI {

        private ClientBossEvent bossInfo;
        private long timeOut;
        private Entity boss;
        private BossCapability cap;

        private ClientBossUI(final ClientBossEvent bossEVent, final Entity boss, final BossCapability cap) {
            this.bossInfo = bossEVent;
            this.timeOut = boss.level().getGameTime() + 20 * 30;
            this.boss = boss;
            this.cap = cap;
        }
    }
}
