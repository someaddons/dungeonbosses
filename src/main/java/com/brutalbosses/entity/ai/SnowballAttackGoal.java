package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class SnowballAttackGoal extends SimpleRangedAttackGoal
{
    public static final ResourceLocation ID = new ResourceLocation("brutalbosses:shootsnowballs");

    private static final double AIM_HEIGHT                     = 2.0D;
    private static final double ARROW_SPEED                    = 1.0D;
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.18;
    private static final double SPEED_FOR_DIST                 = 35;

    public SnowballAttackGoal(final MobEntity mob, final IAIParams params)
    {
        super(mob, params);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    protected ProjectileEntity createProjectile()
    {
        SnowballEntity snowballentity = new SnowballEntity(mob.level, mob);
        ((IOnProjectileHit) snowballentity).setMaxLifeTime(mob.level.getGameTime() + 20 * 40);
        return snowballentity;
    }

    @Override
    protected void doRangedAttack(@Nullable final ProjectileEntity snowballentity, final LivingEntity target)
    {
        snowballentity.noPhysics = false;
        snowballentity.setNoGravity(false);
        positionProjectile(snowballentity, 1);
        final double xVector = target.getX() - snowballentity.getX();
        final double yVector = target.getBoundingBox().minY + target.getBbHeight() / AIM_HEIGHT - snowballentity.getY();
        final double zVector = target.getZ() - snowballentity.getZ();
        final double distance = MathHelper.sqrt(xVector * xVector + zVector * zVector);
        final double dist3d = MathHelper.sqrt(yVector * yVector + xVector * xVector + zVector * zVector);
        snowballentity.shoot(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) (ARROW_SPEED * 1 + (dist3d / SPEED_FOR_DIST)), (float) 3.0f);
        mob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));

        if (snowballentity instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) snowballentity).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
            ((IOnProjectileHit) snowballentity).setOnHitAction(rayTraceResult ->
            {
                if (rayTraceResult instanceof EntityRayTraceResult)
                {
                    final Entity hitEntity = ((EntityRayTraceResult) rayTraceResult).getEntity();
                    if (hitEntity instanceof LivingEntity)
                    {
                        hitEntity.hurt(DamageSource.thrown(snowballentity, mob), 1);
                        ((LivingEntity) hitEntity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 4));
                    }
                }
            });
        }
    }
}
