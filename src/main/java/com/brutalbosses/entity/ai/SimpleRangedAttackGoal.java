package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import com.brutalbosses.entity.PosUtil;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleRangedAttackGoal extends Goal
{
    protected final MobEntity              mob;
    protected       LivingEntity           target;
    protected       int                    attackTime         = -1;
    protected       double                 speedModifier;
    protected       int                    seeTime;
    protected       int                    attackIntervalMin;
    protected       int                    attackIntervalMax;
    protected       float                  attackRadiusSqr;
    protected final BossCapability         cap;
    protected final RangedParams           params;
    private final   List<ProjectileEntity> projectileEntities = new ArrayList<>();
    private         boolean                isChargingUp       = false;
    private final   EntityPredicate        playerAoeFinder;

    public SimpleRangedAttackGoal(MobEntity mob)
    {
        this.mob = mob;
        cap = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
        params = (RangedParams) cap.getBossType().getAIParams(this.getID());
        this.speedModifier = 1.0f;
        this.attackIntervalMin = (params.interval - 10);
        this.attackIntervalMax = (params.interval + 10);
        this.attackRadiusSqr = params.distance * params.distance;
        playerAoeFinder = new EntityPredicate().range(params.distance);
    }

    protected abstract ResourceLocation getID();

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            this.target = target;
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean canContinueToUse()
    {
        return this.canUse();
    }

    public void stop()
    {
        this.target = null;
        this.seeTime = 0;

        for (final ProjectileEntity projectileEntity : projectileEntities)
        {
            projectileEntity.remove();
        }
        projectileEntities.clear();
    }

    public void tick()
    {
        double distSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean canSee = this.mob.getSensing().canSee(this.target);
        if (canSee)
        {
            ++this.seeTime;
        }
        else
        {
            this.seeTime = 0;
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        float relativeAttackDist;

        if (!projectileEntities.isEmpty())
        {
            for (int i = 0; i < projectileEntities.size(); i++)
            {
                final ProjectileEntity projectileEntity = projectileEntities.get(i);
                if (projectileEntity != null)
                {
                    if (!projectileEntity.isAlive())
                    {
                        projectileEntities.remove(i);
                        i--;
                    }
                    else
                    {
                        positionProjectile(projectileEntity, i + 1);
                    }
                }
            }
        }

        if (--this.attackTime == 0)
        {
            if (!canSee || distSqr > (double) this.attackRadiusSqr + 2)
            {
                attackTime = 10;
                return;
            }

            if (projectileEntities.isEmpty())
            {
                isChargingUp = true;
            }

            if (isChargingUp && projectileEntities.size() < params.count)
            {
                mob.getNavigation().stop();

                final int nextCount = projectileEntities.size() + 1;
                projectileEntities.add(createProjectile(nextCount));

                if (params.count > 1)
                {
                    mob.level.playSound(null,
                      mob.getX(),
                      mob.getY(),
                      mob.getZ(),
                      SoundEvents.FIRECHARGE_USE,
                      mob.getSoundSource(),
                      0.3F, 2F);
                }

                // Charge one projectile every 0.25s
                if (nextCount == params.count)
                {
                    if (mob instanceof SpellcastingIllagerEntity)
                    {
                        ((SpellcastingIllagerEntity) mob).setIsCastingSpell(SpellcastingIllagerEntity.SpellType.NONE);
                    }
                    isChargingUp = false;
                    // Delay before shooting
                    attackTime = 5 * params.count;
                }
                else
                {
                    if (mob instanceof SpellcastingIllagerEntity)
                    {
                        ((SpellcastingIllagerEntity) mob).setIsCastingSpell(SpellcastingIllagerEntity.SpellType.WOLOLO);
                    }
                    attackTime = 5;
                }
                return;
            }

            if (params.count > 1 && projectileEntities.size() % 2 == 0)
            {
                final PlayerEntity closest = mob.level.getNearestPlayer(mob, params.distance);
                if (closest != null)
                {
                    target = closest;
                }
            }

            final ProjectileEntity projectileEntity = projectileEntities.remove(projectileEntities.size() - 1);

            if (params.aoe)
            {
                projectileEntity.remove();
                List<PlayerEntity> players =
                  mob.level.getNearbyEntities(PlayerEntity.class, playerAoeFinder, mob, mob.getBoundingBox().inflate(params.distance, 15, params.distance));
                boolean containedTarget = false;

                for (final PlayerEntity playerEntity : players)
                {
                    final ProjectileEntity aoe = createProjectile();
                    aoe.setPos(projectileEntity.getX(), projectileEntity.getY(), projectileEntity.getZ());
                    doRangedAttack(aoe, playerEntity);
                    if (playerEntity == target)
                    {
                        containedTarget = true;
                    }
                }

                if (!containedTarget)
                {
                    final ProjectileEntity aoe = createProjectile();
                    aoe.setPos(projectileEntity.getX(), projectileEntity.getY(), projectileEntity.getZ());
                    doRangedAttack(aoe, target);
                }
            }
            else
            {
                doRangedAttack(projectileEntity, target);
            }

            if (!projectileEntities.isEmpty())
            {
                attackTime = 10;
                return;
            }

            relativeAttackDist = MathHelper.sqrt(distSqr) / this.params.distance;
            this.attackTime = MathHelper.floor(
              (1 + BrutalBosses.rand.nextFloat() * 0.5f) * (relativeAttackDist * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin))
                                - 5 * params.count;
        }
        else if (this.attackTime < 0)
        {
            relativeAttackDist = MathHelper.sqrt(distSqr) / this.params.distance;
            this.attackTime = MathHelper.floor(
              (1 + BrutalBosses.rand.nextFloat() * 0.5f) * (relativeAttackDist * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin))
                                - 5 * params.count;
        }
    }

    /**
     * Creator for a fake projectile entity
     *
     * @return
     */
    protected abstract ProjectileEntity createProjectile();

    protected abstract void doRangedAttack(final ProjectileEntity projectileEntity, final LivingEntity target);

    /**
     * Creates a projectile for the given number and positions it
     *
     * @param number
     * @return
     */
    protected ProjectileEntity createProjectile(int number)
    {
        final ProjectileEntity projectileEntity = createProjectile();

        positionProjectile(projectileEntity, number);
        projectileEntity.noPhysics = true;
        projectileEntity.setNoGravity(true);
        projectileEntity.setOwner(mob);
        if (projectileEntity instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) projectileEntity).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
        }

        mob.level.addFreshEntity(projectileEntity);
        return projectileEntity;
    }

    /**
     * Positions the projectiles in a circle around the entity
     *
     * @param projectileEntity
     * @param number
     */
    protected void positionProjectile(final ProjectileEntity projectileEntity, final int number)
    {
        if (params.count == 1)
        {
            final Direction dir = PosUtil.getFacing(mob.position(), target.position()).getClockWise();
            projectileEntity.setPos(mob.getX() + dir.getCounterClockWise().getStepX() * 0.5,
              mob.getY() + mob.getEyeHeight() - 0.5,
              mob.getZ() + dir.getCounterClockWise().getStepZ() * 0.5);
            return;
        }

        final Direction dir = PosUtil.getFacing(mob.position(), target.position()).getClockWise();

        Vector3d center = new Vector3d(mob.getX(), mob.getY() - 0.5 + mob.getEyeHeight() * cap.getBossType().getVisualScale(), mob.getZ());

        double y = 0.75d * Math.cos(Math.toRadians((360d / (params.count + 1)) * (number)));
        double xzRatio = 0.75d * Math.sin(Math.toRadians((360d / (params.count + 1)) * (number)));

        Vector3d offSet = new Vector3d(xzRatio * dir.getStepX(), -y, xzRatio * dir.getStepZ());

        center = center.add(offSet);
        center = center.add(dir.getCounterClockWise().getStepX() * 0.5, 0, dir.getCounterClockWise().getStepZ() * 0.5);
        projectileEntity.setPos(center.x, center.y, center.z);
    }

    public static final String PROJECTILE_COUNT    = "projectile_count";
    public static final String PROJECTILE_INTERVAL = "projectile_interval";
    public static final String PROJECTILE_DISTANCE = "projectile_distance";
    public static final String PROJECTILE_AOE      = "projectile_aoe";

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    public static IAIParams parse(final JsonObject jsonElement)
    {
        return parse(jsonElement, null);
    }

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    protected static IAIParams parse(final JsonObject jsonElement, RangedParams params)
    {
        if (params == null)
        {
            params = new RangedParams();
        }

        if (jsonElement.has(PROJECTILE_COUNT))
        {
            params.count = jsonElement.get(PROJECTILE_COUNT).getAsInt();
        }

        if (jsonElement.has(PROJECTILE_INTERVAL))
        {
            params.interval = jsonElement.get(PROJECTILE_INTERVAL).getAsInt();
        }

        if (jsonElement.has(PROJECTILE_DISTANCE))
        {
            params.distance = jsonElement.get(PROJECTILE_DISTANCE).getAsInt();
        }
        if (jsonElement.has(PROJECTILE_AOE))
        {
            params.aoe = jsonElement.get(PROJECTILE_AOE).getAsBoolean();
        }

        return params;
    }

    protected static class RangedParams implements IAIParams
    {
        protected int     count    = 1;
        protected int     interval = 50;
        protected int     distance = 15;
        protected boolean aoe      = false;

        protected RangedParams()
        {
        }
    }
}
