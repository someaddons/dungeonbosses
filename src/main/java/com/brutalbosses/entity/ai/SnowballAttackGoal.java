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

    public SnowballAttackGoal(final MobEntity mob)
    {
        super(mob);
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
        return snowballentity;
    }

    @Override
    protected void doRangedAttack(@Nullable final ProjectileEntity snowballentity, final LivingEntity target)
    {
        snowballentity.noPhysics = false;
        positionProjectile(snowballentity, 1);
        double eyeHeight = target.getEyeY() - (double) 1.1F;
        double xDiff = target.getX() - snowballentity.getX();
        double yDiff = eyeHeight - snowballentity.getY();
        double zDiff = target.getZ() - snowballentity.getZ();
        float f = MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff) * 0.2F;
        snowballentity.shoot(xDiff, yDiff + (double) f, zDiff, 1.6F, 3.0F);
        mob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));

        if (snowballentity instanceof IOnProjectileHit)
        {
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
