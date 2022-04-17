package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;

public class BigFireballAttackGoal extends SimpleRangedAttackGoal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:shootbigfireballs");

    public BigFireballAttackGoal(final Mob mob, final IAIParams params)
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
        double yDiff = target.getY(0.5D) - mob.getY(0.5D);
        double zDiff = target.getZ() - mob.getZ();

        float distVariance = (float) (Math.sqrt(Math.sqrt(this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ()))) * 0.5F);

        final LargeFireball fireballEntity = new LargeFireball(mob.level,
          mob,
          xDiff + mob.getRandom().nextGaussian() * (double) distVariance,
          yDiff,
          zDiff + mob.getRandom().nextGaussian() * (double) distVariance, 2);
        fireballEntity.setPos(mob.getX(), mob.getY() + mob.getEyeHeight() - 0.5, mob.getZ());
        fireballEntity.setOwner(mob);
        fireballEntity.setRemainingFireTicks(10000);
        ((IOnProjectileHit) fireballEntity).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
        mob.level.addFreshEntity(fireballEntity);
    }

    @Override
    protected Projectile createProjectile()
    {
        final LargeFireball fireballEntity = new LargeFireball(mob.level,
          mob,
          0,
          0,
          0, 2);
        fireballEntity.setRemainingFireTicks(10000);
        ((IOnProjectileHit) fireballEntity).setMaxLifeTime(mob.level.getGameTime() + 20 * 40);
        return fireballEntity;
    }
}
