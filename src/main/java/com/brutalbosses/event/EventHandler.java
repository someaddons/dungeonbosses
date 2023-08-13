package com.brutalbosses.event;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.CustomAttributes;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.BossTypeSyncMessage;
import com.brutalbosses.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.brutalbosses.entity.CustomAttributes.DROP_GEAR;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler {

    public static Map<BlockPos, UUID> protectedBlocks = new HashMap<>();

    public static void onPlayerInteract(final Player player, final BlockPos pos, final CallbackInfoReturnable<InteractionResult> cir) {
        if (player.getLevel().isClientSide()) {
            return;
        }

        if (protectedBlocks.containsKey(pos)) {
            final UUID uuid = protectedBlocks.get(pos);

            final Entity boss = ((ServerLevel) player.level).getEntity(uuid);
            if (boss instanceof LivingEntity) {
                if (boss.isAlive()) {
                    ((LivingEntity) boss).addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60));

                    if (boss instanceof Mob && ((Mob) boss).getTarget() == null) {
                        ((Mob) boss).setTarget(player);
                    }

                    ((ServerPlayer) player).sendSystemMessage(Component.translatable("boss.chest.lock",
                            boss.getDisplayName()).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }

        if (!BrutalBosses.config.getCommonConfig().printChestLoottable || player.getLevel().isClientSide()) {
            return;
        }

        final BlockEntity te = player.level.getBlockEntity(pos);
        if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null) {
            player
                    .sendSystemMessage(Component.literal("[Loottable: " + ((RandomizableContainerBlockEntity) te).lootTable + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                                    ((RandomizableContainerBlockEntity) te).lootTable.toString()))));
        }
    }

    public static float applyProjectileDamageBoost(final DamageSource damageSource, final LivingEntity attackedEntity, final float f) {

        if (attackedEntity.level.isClientSide)
        {
            return f;
        }

        if (damageSource.getEntity() instanceof ServerPlayer && attackedEntity instanceof BossCapEntity) {
            final BossCapability cap = ((BossCapEntity) attackedEntity).getBossCap();
            if (cap != null && cap.isBoss()) {
                Network.instance.sendPacket((ServerPlayer) damageSource.getEntity(), new BossOverlayMessage(attackedEntity.getId()));
            }
            return f;
        }

        if (damageSource instanceof IndirectEntityDamageSource && damageSource.getEntity() instanceof BossCapEntity) {
            final BossCapability cap = ((BossCapEntity) damageSource.getEntity()).getBossCap();
            if (cap != null && cap.isBoss()) {
                return (float) (f + (cap.getBossType().getCustomAttributeValueOrDefault(CustomAttributes.PROJECTILE_DAMAGE, 0)
                        * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier));
            }
        }

        return f;
    }

    public static void onBossDeath(final LivingEntity entity, final BossCapability cap, DamageSource damageSource) {
        if (!entity.level.isClientSide() && damageSource.getEntity() instanceof ServerPlayer) {
            int exp = cap.getBossType().getExperienceDrop();
            while (exp > 0) {
                int orbValue = ExperienceOrb.getExperienceValue(exp);
                exp -= orbValue;
                entity.level.addFreshEntity(new ExperienceOrb(entity.level,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        orbValue));
            }

            final int gearDropCount = Math.min(EquipmentSlot.values().length, (int) cap.getBossType().getCustomAttributeValueOrDefault(DROP_GEAR, 0));

            for (int i = 0; i < gearDropCount; i++) {
                final ItemEntity itementity =
                        new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(),
                                entity.getItemBySlot(EquipmentSlot.values()[i]));
                entity.level.addFreshEntity(itementity);
            }

            if (cap.getLootTable() != null) {
                final LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel) entity.level))
                        .withParameter(LootContextParams.ORIGIN, entity.position())
                        .withParameter(LootContextParams.THIS_ENTITY, damageSource.getEntity())
                        .withLuck(((ServerPlayer) damageSource.getEntity()).getLuck());

                final LootTable loottable = entity.level.getServer().getLootTables().get(cap.getLootTable());
                final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootContextParamSets.CHEST));

                if (list.isEmpty()) {
                    return;
                }

                for (int i = 0; i < cap.getBossType().getItemLootCount(); i++) {
                    final ItemEntity itementity =
                            new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), list.get(
                                    BrutalBosses.rand.nextInt(list.size())));
                    entity.level.addFreshEntity(itementity);
                }
            }

        }
    }

    public static void onTrack(final BossCapEntity entity, final Player player) {
        if (player instanceof ServerPlayer) {
            final BossCapability bossCapability = entity.getBossCap();
            if (bossCapability != null && bossCapability.isBoss()) {
                Network.instance.sendPacket((ServerPlayer) player, new BossCapMessage(bossCapability));
            }
        }
    }

    public static void onPlayerLogin(final ServerPlayer player) {
        if (player.getServer() instanceof DedicatedServer) {
            Network.instance.sendPacket(player, new BossTypeSyncMessage(BossTypeManager.instance.bosses.values()));
        }
    }
}
