package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;

public class SpitCobwebGoal extends SimpleRangedAttackGoal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:spitcobweb");

    public SpitCobwebGoal(final MobEntity mob, final IAIParams params)
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
            if (mob instanceof SpiderEntity && target.getY() <= mob.getY())
            {
                ((SpiderEntity) mob).setClimbing(false);
            }
        }
        return canUse;
    }

    @Override
    protected ProjectileEntity createProjectile()
    {
        final LlamaSpitEntity spitEntity = EntityType.LLAMA_SPIT.create(mob.level);
        return spitEntity;
    }

    @Override
    protected void positionProjectile(final ProjectileEntity projectileEntity, final int number)
    {
        projectileEntity.setPos(mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ());
    }

    @Override
    protected void doRangedAttack(ProjectileEntity projectileEntity, final LivingEntity target)
    {
        projectileEntity.remove();
        projectileEntity = createProjectile();
        projectileEntity.setNoGravity(true);
        positionProjectile(projectileEntity, 1);
        mob.level.addFreshEntity(projectileEntity);

        double xDiff = target.getX() - mob.getX();
        double yDiff = target.getY(0.3333333333333333D) - projectileEntity.getY();
        double zDiff = target.getZ() - mob.getZ();
        float f = MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff) * 0.2F;
        projectileEntity.shoot(xDiff, yDiff + (double) f, zDiff, 0.6F * params.speed, 10.0F);
        mob.level.playSound((PlayerEntity) null,
          mob.getX(),
          mob.getY(),
          mob.getZ(),
          SoundEvents.LLAMA_SPIT,
          mob.getSoundSource(),
          1.0F,
          2F + (BrutalBosses.rand.nextFloat() - BrutalBosses.rand.nextFloat()) * 0.2F);

        if (projectileEntity instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) projectileEntity).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
            ((IOnProjectileHit) projectileEntity).setOnHitAction(rayTraceResult ->
            {
                if (rayTraceResult instanceof EntityRayTraceResult)
                {
                    final Entity hitEntity = ((EntityRayTraceResult) rayTraceResult).getEntity();
                    if (hitEntity != null)
                    {
                        trySpawnCobweb(hitEntity.blockPosition());
                    }
                }
                else if (rayTraceResult instanceof BlockRayTraceResult)
                {
                    final BlockPos hitPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
                    trySpawnCobweb(hitPos.relative(((BlockRayTraceResult) rayTraceResult).getDirection(), 1));
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
