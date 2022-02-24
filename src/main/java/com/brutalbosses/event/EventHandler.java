package com.brutalbosses.event;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossJsonListener;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.CustomAttributes;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.BossTypeSyncMessage;
import com.brutalbosses.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

import static com.brutalbosses.entity.CustomAttributes.DROP_GEAR;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    /*
    @SubscribeEvent
    public static void entitySizeChange(final EntityEvent.Size event)
    {
        // Scale bb only on client side?
        if (event.getEntity() instanceof CustomEntityRenderData && event.getEntity().level.isClientSide())
        {
            final BossCapability cap = event.getEntity().getCapability(BossTypeManager.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {m
                event.setNewSize(event.getOldSize().scale(((CustomEntityRenderData) event.getEntity()).getVisualScale()));
            }
        }
    }
     */

    @SubscribeEvent
    public static void applyProjectileDamageBoost(final LivingHurtEvent event)
    {
        if (event.getSource().getEntity() instanceof Player && event.getEntity() instanceof Mob)
        {
            final BossCapability cap = event.getEntity().getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {
                Network.instance.sendPacket((ServerPlayer) event.getSource().getEntity(), new BossOverlayMessage(event.getEntity().getId()));
            }
            return;
        }

        if (event.getSource() instanceof IndirectEntityDamageSource && event.getSource().getEntity() != null)
        {
            final BossCapability cap = event.getSource().getEntity().getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {
                event.setAmount((float) ((event.getAmount() + cap.getBossType().getCustomAttributeValueOrDefault(CustomAttributes.PROJECTILE_DAMAGE, 0))
                                           * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier.get()));
            }
        }
    }

    @SubscribeEvent
    public static void playerClickBlockEvent(final PlayerInteractEvent.RightClickBlock event)
    {
        if (!BrutalBosses.config.getCommonConfig().printChestLoottable.get() || event.getWorld().isClientSide())
        {
            return;
        }

        final BlockEntity te = event.getEntity().level.getBlockEntity(event.getPos());
        if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null)
        {
            event.getPlayer()
              .sendMessage(new TextComponent("[Loottable: " + ((RandomizableContainerBlockEntity) te).lootTable + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                  .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                    ((RandomizableContainerBlockEntity) te).lootTable.toString()))),
                event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onBossDeath(final LivingDeathEvent event)
    {
        if (!event.getEntity().level.isClientSide() && event.getSource().getEntity() instanceof ServerPlayer)
        {
            final BossCapability cap = event.getEntity().getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {
                int exp = cap.getBossType().getExperienceDrop();
                while (exp > 0)
                {
                    int orbValue = ExperienceOrb.getExperienceValue(exp);
                    exp -= orbValue;
                    event.getEntity().level.addFreshEntity(new ExperienceOrb(event.getEntity().level,
                      event.getEntity().getX(),
                      event.getEntity().getY(),
                      event.getEntity().getZ(),
                      orbValue));
                }

                final int gearDropCount = Math.min(EquipmentSlot.values().length, (int) cap.getBossType().getCustomAttributeValueOrDefault(DROP_GEAR, 0));

                for (int i = 0; i < gearDropCount; i++)
                {
                    final ItemEntity itementity =
                      new ItemEntity(event.getEntity().level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        event.getEntityLiving().getItemBySlot(EquipmentSlot.values()[i]));
                    event.getEntity().level.addFreshEntity(itementity);
                }

                if (cap.getLootTable() != null)
                {
                    final LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel) event.getEntity().level))
                      .withParameter(LootContextParams.ORIGIN, event.getEntity().position())
                      .withParameter(LootContextParams.THIS_ENTITY, event.getSource().getEntity())
                      .withLuck(((ServerPlayer) event.getSource().getEntity()).getLuck());

                    final LootTable loottable = event.getEntity().level.getServer().getLootTables().get(cap.getLootTable());
                    final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootContextParamSets.CHEST));

                    if (list.isEmpty())
                    {
                        return;
                    }

                    for (int i = 0; i < cap.getBossType().getItemLootCount(); i++)
                    {
                        final ItemEntity itementity =
                          new ItemEntity(event.getEntity().level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), list.get(
                            BrutalBosses.rand.nextInt(list.size())));
                        event.getEntity().level.addFreshEntity(itementity);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> evt)
    {
        final Entity entity = evt.getObject();

        if (entity.level.isClientSide || BossTypeManager.instance.isValidBossEntity(entity))
        {
            evt.addCapability(BossCapability.ID, new BossCapability(entity));
        }
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event)
    {
        event.addListener(BossJsonListener.instance);
    }

    @SubscribeEvent
    public static void onTrack(PlayerEvent.StartTracking event)
    {
        final Entity entity = event.getTarget();
        final Player Player = event.getPlayer();

        if (Player instanceof ServerPlayer)
        {
            final BossCapability bossCapability = entity.getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (bossCapability != null)
            {
                Network.instance.sendPacket((ServerPlayer) Player, new BossCapMessage(bossCapability));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () ->
        {
            Network.instance.sendPacket((ServerPlayer) event.getPlayer(), new BossTypeSyncMessage(BossTypeManager.instance.bosses.values()));
        });
    }
}
