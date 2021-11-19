package com.brutalbosses.event;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossJsonListener;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.CustomAttributes;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.Network;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (event.getSource().getEntity() instanceof PlayerEntity && event.getEntity() instanceof MobEntity)
        {
            final BossCapability cap = event.getEntity().getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {
                Network.instance.sendPacket((ServerPlayerEntity) event.getSource().getEntity(), new BossOverlayMessage(event.getEntity().getId()));
            }
            return;
        }

        if (event.getSource() instanceof IndirectEntityDamageSource)
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
        if (!BrutalBosses.config.getCommonConfig().printChestLoottableOnOpen.get() || event.getWorld().isClientSide())
        {
            return;
        }

        final TileEntity te = event.getEntity().level.getBlockEntity(event.getPos());
        if (te instanceof LockableLootTileEntity && ((LockableLootTileEntity) te).lootTable != null)
        {
            event.getPlayer()
              .sendMessage(new StringTextComponent("[Loottable: " + ((LockableLootTileEntity) te).lootTable + "]").setStyle(Style.EMPTY.withColor(TextFormatting.GOLD)
                                                                                                                              .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                                                                                                ((LockableLootTileEntity) te).lootTable.toString()))),
                event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onBossDeath(final LivingDeathEvent event)
    {
        if (!event.getEntity().level.isClientSide() && event.getSource().getEntity() instanceof ServerPlayerEntity)
        {
            final BossCapability cap = event.getEntity().getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {
                int exp = cap.getBossType().getExperienceDrop();
                while (exp > 0)
                {
                    int orbValue = ExperienceOrbEntity.getExperienceValue(exp);
                    exp -= orbValue;
                    event.getEntity().level.addFreshEntity(new ExperienceOrbEntity(event.getEntity().level,
                      event.getEntity().getX(),
                      event.getEntity().getY(),
                      event.getEntity().getZ(),
                      orbValue));
                }

                final int gearDropCount = (int) cap.getBossType().getCustomAttributeValueOrDefault(DROP_GEAR, 0);

                for (int i = 0; i < gearDropCount; i++)
                {
                    final ItemEntity itementity =
                      new ItemEntity(event.getEntity().level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        event.getEntityLiving().getItemBySlot(EquipmentSlotType.values()[i]));
                    event.getEntity().level.addFreshEntity(itementity);
                }

                if (cap.getLootTable() != null)
                {
                    final LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) event.getEntity().level))
                                                                      .withParameter(LootParameters.ORIGIN, event.getEntity().position())
                                                                      .withParameter(LootParameters.THIS_ENTITY, event.getSource().getEntity())
                                                                      .withLuck(((ServerPlayerEntity) event.getSource().getEntity()).getLuck());

                    final LootTable loottable = event.getEntity().level.getServer().getLootTables().get(cap.getLootTable());
                    final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootParameterSets.CHEST));

                    if (list.isEmpty())
                    {
                        return;
                    }

                    for (int i = 0; i < 3; i++)
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

        if (BossTypeManager.instance.isValidBossEntity(entity))
        {
            evt.addCapability(BossCapability.ID, new BossCapability(entity));
        }
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event)
    {
        event.addListener(new BossJsonListener());
    }

    @SubscribeEvent
    public static void onTrack(PlayerEvent.StartTracking event)
    {
        final Entity entity = event.getTarget();
        final PlayerEntity playerEntity = event.getPlayer();

        if (playerEntity instanceof ServerPlayerEntity)
        {
            final BossCapability bossCapability = entity.getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (bossCapability != null)
            {
                Network.instance.sendPacket((ServerPlayerEntity) playerEntity, new BossCapMessage(bossCapability));
            }
        }
    }
}
