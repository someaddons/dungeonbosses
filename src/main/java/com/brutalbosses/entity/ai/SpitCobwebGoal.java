package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SpitCobwebGoal extends SimpleRangedAttackGoal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:spitcobweb");

    public SpitCobwebGoal(final Mob mob, final IAIParams params)
    {
        super(mob, params);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public boolean canUse()
    {
        final boolean canUse = super.canUse();
        if (canUse)
        {
            if (mob instanceof Spider && target.getY() <= mob.getY())
            {
                ((Spider) mob).setClimbing(false);
            }
        }
        return canUse;
    }

    @Override
    protected Projectile createProjectile()
    {
        final LlamaSpit spitEntity = EntityType.LLAMA_SPIT.create(mob.level);
        return spitEntity;
    }

    @Override
    protected void positionProjectile(final Projectile projectile, final int number)
    {
        projectile.setPos(mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ());
    }

    @Override
    protected void doRangedAttack(Projectile projectile, final LivingEntity target)
    {
        projectile.discard();
        projectile = createProjectile();
        projectile.setNoGravity(true);
        positionProjectile(projectile, 1);
        mob.level.addFreshEntity(projectile);

        double xDiff = target.getX() - mob.getX();
        double yDiff = target.getY(0.3333333333333333D) - projectile.getY();
        double zDiff = target.getZ() - mob.getZ();
        float f = (float) (Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 0.2F);
        projectile.shoot(xDiff, yDiff + (double) f, zDiff, 0.9F * params.speed, 10.0F);
        mob.level.playSound((Player) null,
          mob.getX(),
          mob.getY(),
          mob.getZ(),
          SoundEvents.LLAMA_SPIT,
          mob.getSoundSource(),
          1.0F,
          2F + (BrutalBosses.rand.nextFloat() - BrutalBosses.rand.nextFloat()) * 0.2F);

        if (projectile instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) projectile).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
            ((IOnProjectileHit) projectile).setOnHitAction(rayTraceResult ->
            {
                if (rayTraceResult instanceof EntityHitResult)
                {
                    final Entity hitEntity = ((EntityHitResult) rayTraceResult).getEntity();
                    if (hitEntity != null)
                    {
                        trySpawnCobweb(hitEntity.blockPosition());
                    }
                }
                else if (rayTraceResult instanceof BlockHitResult)
                {
                    final BlockPos hitPos = ((BlockHitResult) rayTraceResult).getBlockPos();
                    trySpawnCobweb(hitPos.relative(((BlockHitResult) rayTraceResult).getDirection(), 1));
                }
            });
        }
    }

    private void trySpawnCobweb(final BlockPos blockPosition)
    {
        if (mob.level.getBlockState(blockPosition).getMaterial() == Material.AIR)
        {
            mob.level.setBlock(blockPosition, Blocks.COBWEB.defaultBlockState(), 3);
        }
    }
}
