package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;

public class SnowballAttackGoal extends SimpleRangedAttackGoal
{
    public static final ResourceLocation ID = new ResourceLocation("brutalbosses:shootsnowballs");

    private static final double AIM_HEIGHT                     = 2.0D;
    private static final double ARROW_SPEED                    = 1.0D;
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.18;
    private static final double SPEED_FOR_DIST                 = 35;

    public SnowballAttackGoal(final Mob mob, final IAIParams params)
    {
        super(mob, params);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    protected Projectile createProjectile()
    {
        Snowball snowballentity = new Snowball(mob.level(), mob);
        ((IOnProjectileHit) snowballentity).setMaxLifeTime(mob.level().getGameTime() + 20 * 40);
        return snowballentity;
    }

    @Override
    protected void doRangedAttack(final Projectile snowballentity, final LivingEntity target)
    {
        snowballentity.noPhysics = false;
        snowballentity.setNoGravity(false);
        positionProjectile(snowballentity, 1);
        final double xVector = target.getX() - snowballentity.getX();
        final double yVector = target.getBoundingBox().minY + target.getBbHeight() / AIM_HEIGHT - snowballentity.getY();
        final double zVector = target.getZ() - snowballentity.getZ();
        final double distance = Math.sqrt(xVector * xVector + zVector * zVector);
        final double dist3d = Math.sqrt(yVector * yVector + xVector * xVector + zVector * zVector);
        snowballentity.shoot(xVector,
          yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER,
          zVector,
          (float) (ARROW_SPEED * 1 + (dist3d / SPEED_FOR_DIST)) * params.speed,
          (float) 3.0f);
        mob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));

        if (snowballentity instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) snowballentity).setMaxLifeTime(mob.level().getGameTime() + 20 * 20);
            ((IOnProjectileHit) snowballentity).setOnHitAction(rayTraceResult ->
            {
                if (rayTraceResult instanceof EntityHitResult)
                {
                    final Entity hitEntity = ((EntityHitResult) rayTraceResult).getEntity();
                    if (hitEntity instanceof LivingEntity)
                    {
                        hitEntity.hurt(mob.damageSources().thrown(snowballentity, mob), 1);
                        ((LivingEntity) hitEntity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
                    }
                }
            });
        }
    }
}
