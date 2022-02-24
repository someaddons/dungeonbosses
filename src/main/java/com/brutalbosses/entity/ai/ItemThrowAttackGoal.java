package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.IOnProjectileHit;
import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ItemThrowAttackGoal extends SimpleRangedAttackGoal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:itemshootgoal");

    public ItemThrowAttackGoal(final Mob mob, final IAIParams params)
    {
        super(mob, params);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    protected void doRangedAttack(final Projectile Projectile, final LivingEntity target)
    {
        Projectile.remove(Entity.RemovalReason.DISCARDED);

        double xDiff = target.getX() - mob.getX();
        double yDiff = target.getY(0.5D) - (mob.getY() + mob.getEyeHeight() - 0.5);
        double zDiff = target.getZ() - mob.getZ();

        final ThrownItemEntity pearlEntity = new ThrownItemEntity(mob.level, mob);
        pearlEntity.setPos(mob.getX(), mob.getY() + mob.getEyeHeight() - 0.5, mob.getZ());
        pearlEntity.shoot(xDiff, yDiff, zDiff, 0.8F, 3.0F);

        pearlEntity.setItem(((ItemThrowParams) params).item);
        pearlEntity.setNoGravity(true);
        if (pearlEntity instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) pearlEntity).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
            ((IOnProjectileHit) pearlEntity).setOnHitAction(rayTraceResult ->
            {
                if (!mob.isAlive())
                {
                    return;
                }

                if (rayTraceResult instanceof EntityHitResult)
                {
                    final Entity hitEntity = ((EntityHitResult) rayTraceResult).getEntity();
                    if (hitEntity instanceof LivingEntity && hitEntity != mob)
                    {
                        if (((ItemThrowParams) params).damage > 0)
                        {
                            hitEntity.hurt(DamageSource.indirectMagic(hitEntity, mob), ((ItemThrowParams) params).damage);
                        }

                        if (((ItemThrowParams) params).lighting)
                        {
                            LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(hitEntity.level);
                            lightningboltentity.moveTo(hitEntity.getX(), hitEntity.getY(), hitEntity.getZ());
                            lightningboltentity.setVisualOnly(false);
                            mob.level.addFreshEntity(lightningboltentity);
                        }

                        if (((ItemThrowParams) params).explode)
                        {
                            hitEntity.level.explode(null,
                              DamageSource.indirectMobAttack((LivingEntity) hitEntity, mob),
                              null,
                              hitEntity.getX(),
                              hitEntity.getY(),
                              hitEntity.getZ(),
                              (float) (1 * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier.get()) * pearlEntity.getScale(),
                              false,
                              Explosion.BlockInteraction.BREAK);
                        }

                        if (((ItemThrowParams) params).teleport)
                        {
                            double d0 = (double) (-Mth.sin(mob.getYRot() * ((float) Math.PI / 180)));
                            double d1 = (double) Mth.cos(mob.getYRot() * ((float) Math.PI / 180));
                            if (mob.level instanceof ServerLevel)
                            {
                                ((ServerLevel) mob.level).sendParticles(ParticleTypes.PORTAL,
                                  mob.getX() + d0,
                                  mob.getY(0.5D),
                                  mob.getZ() + d1,
                                  20,
                                  d0,
                                  0.0D,
                                  d1,
                                  0.0D);
                            }

                            mob.level.playSound((Player) null, mob.xo, mob.yo, mob.zo, SoundEvents.ENDERMAN_TELEPORT, mob.getSoundSource(), 1.0F, 1.0F);
                            mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

                            mob.teleportTo(pearlEntity.getX(), hitEntity.getY(), pearlEntity.getZ());
                        }
                    }
                }
                else if (rayTraceResult instanceof BlockHitResult)
                {
                    final BlockPos hitPos = ((BlockHitResult) rayTraceResult).getBlockPos();
                    if (((ItemThrowParams) params).lighting)
                    {
                        LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(mob.level);
                        lightningboltentity.moveTo(hitPos.getX(), hitPos.getY(), hitPos.getZ());
                        lightningboltentity.setVisualOnly(false);
                        mob.level.addFreshEntity(lightningboltentity);
                    }

                    if (((ItemThrowParams) params).explode)
                    {
                        mob.level.explode(null,
                          DamageSource.indirectMobAttack(mob, mob),
                          null,
                          hitPos.getX(),
                          hitPos.getY(),
                          hitPos.getZ(),
                          (float) (1 * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier.get()),
                          false,
                          Explosion.BlockInteraction.NONE);
                    }

                    if (((ItemThrowParams) params).teleport)
                    {
                        final BlockPos tpPos = BossSpawnHandler.findSpawnPosForBoss((ServerLevelAccessor) mob.level, mob, hitPos);
                        if (tpPos != null)
                        {
                            double d0 = (double) (-Mth.sin(mob.getYRot() * ((float) Math.PI / 180)));
                            double d1 = (double) Mth.cos(mob.getYRot() * ((float) Math.PI / 180));
                            if (mob.level instanceof ServerLevel)
                            {
                                ((ServerLevel) mob.level).sendParticles(ParticleTypes.PORTAL,
                                  mob.getX() + d0,
                                  mob.getY(0.5D),
                                  mob.getZ() + d1,
                                  20,
                                  d0,
                                  0.0D,
                                  d1,
                                  0.0D);
                            }

                            mob.level.playSound((Player) null, mob.xo, mob.yo, mob.zo, SoundEvents.ENDERMAN_TELEPORT, mob.getSoundSource(), 1.0F, 1.0F);
                            mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

                            mob.teleportTo(tpPos.getX(), tpPos.getY(), tpPos.getZ());
                        }
                    }
                }
            });
        }

        mob.level.addFreshEntity(pearlEntity);
    }

    @Override
    protected Projectile createProjectile()
    {
        final ThrownItemEntity pearlEntity = new ThrownItemEntity(mob.level, mob);
        pearlEntity.setItem(((ItemThrowParams) params).item);
        pearlEntity.setScale(((ItemThrowParams) params).projectilesize);
        return pearlEntity;
    }

    public static class ItemThrowParams extends RangedParams
    {
        private ItemStack item           = Items.ENDER_PEARL.getDefaultInstance();
        private float     damage         = 0;
        private boolean   teleport       = false;
        private boolean   lighting       = false;
        private boolean   explode        = false;
        private float     projectilesize = 1.0f;

        public ItemThrowParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String ITEM      = "item";
        private static final String DAMAGE    = "damage";
        private static final String TELEPORT  = "teleport";
        private static final String LIGHTNING = "lightning";
        private static final String EXPLODE   = "explode";
        private static final String PR_SIZE   = "projectilesize";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(ITEM))
            {
                try
                {
                    item = ItemStack.of(TagParser.parseTag(jsonElement.get(ITEM).getAsString()));
                }
                catch (CommandSyntaxException e)
                {
                    BrutalBosses.LOGGER.warn("Could not parse item of: " + jsonElement.get(ITEM).getAsString(), e);
                }
            }

            if (jsonElement.has(DAMAGE))
            {
                damage = jsonElement.get(DAMAGE).getAsFloat();
            }

            if (jsonElement.has(TELEPORT))
            {
                teleport = jsonElement.get(TELEPORT).getAsBoolean();
            }

            if (jsonElement.has(LIGHTNING))
            {
                lighting = jsonElement.get(LIGHTNING).getAsBoolean();
            }

            if (jsonElement.has(EXPLODE))
            {
                explode = jsonElement.get(EXPLODE).getAsBoolean();
            }

            if (jsonElement.has(PR_SIZE))
            {
                projectilesize = jsonElement.get(PR_SIZE).getAsFloat();
            }

            return this;
        }
    }
}
