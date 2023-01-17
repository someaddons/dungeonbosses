package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import com.brutalbosses.entity.PosUtil;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleRangedAttackGoal extends Goal
{
    protected final Mob                 mob;
    protected       LivingEntity        target;
    protected       int                 attackTime         = -1;
    protected       double              speedModifier;
    protected       int                 seeTime;
    protected       int                 attackIntervalMin;
    protected       int                 attackIntervalMax;
    protected       float               attackRadiusSqr;
    protected final BossCapability      cap;
    protected final RangedParams        params;
    private final   List<Projectile>    projectileEntities = new ArrayList<>();
    private         boolean             isChargingUp       = false;
    private final   TargetingConditions playerAoeFinder;

    public SimpleRangedAttackGoal(Mob mob, final IAIParams params)
    {
        this.mob = mob;
        cap = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
        this.params = (RangedParams) params;
        this.speedModifier = 1.0f;
        this.attackIntervalMin = (this.params.interval - 10);
        this.attackIntervalMax = (this.params.interval + 10);
        this.attackRadiusSqr = this.params.distance * this.params.distance;
        playerAoeFinder = TargetingConditions.forCombat().range(this.params.distance);
        attackTime = ((RangedParams) params).interval / 2;
    }

    protected abstract ResourceLocation getID();

    public boolean canUse()
    {
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            this.target = target;
            return params.healthPhaseCheck.test(mob);
        }

        target = mob.getLastHurtByMob();
        if (target != null && target.isAlive())
        {
            this.target = target;
            return params.healthPhaseCheck.test(mob);
        }

        return false;
    }

    public boolean canContinueToUse()
    {
        return this.canUse();
    }

    public void stop()
    {
        this.target = null;
        this.seeTime = 0;

        for (final Projectile Projectile : projectileEntities)
        {
            Projectile.remove(Entity.RemovalReason.DISCARDED);
        }
        projectileEntities.clear();
        attackTime = params.interval / 2;
    }

    public void tick()
    {
        double distSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean canSee = this.mob.getSensing().hasLineOfSight(this.target);
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
                final Projectile Projectile = projectileEntities.get(i);
                if (Projectile != null)
                {
                    if (!Projectile.isAlive())
                    {
                        projectileEntities.remove(i);
                        i--;
                    }
                    else
                    {
                        positionProjectile(Projectile, i + 1);
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
                    if (mob instanceof SpellcasterIllager)
                    {
                        ((SpellcasterIllager) mob).setIsCastingSpell(SpellcasterIllager.IllagerSpell.NONE);
                    }
                    isChargingUp = false;
                    // Delay before shooting
                    attackTime = 5 * params.count;
                }
                else
                {
                    if (mob instanceof SpellcasterIllager)
                    {
                        ((SpellcasterIllager) mob).setIsCastingSpell(SpellcasterIllager.IllagerSpell.WOLOLO);
                    }
                    attackTime = 5;
                }
                return;
            }

            if (params.count > 1 && projectileEntities.size() % 2 == 0)
            {
                final Player closest = mob.level.getNearestPlayer(mob, params.distance);
                if (closest != null)
                {
                    target = closest;
                }
            }

            final Projectile Projectile = projectileEntities.remove(projectileEntities.size() - 1);

            if (params.aoe)
            {
                Projectile.remove(Entity.RemovalReason.DISCARDED);
                List<Player> players =
                  mob.level.getNearbyEntities(Player.class, playerAoeFinder, mob, mob.getBoundingBox().inflate(params.distance, 15, params.distance));
                boolean containedTarget = false;

                for (final Player Player : players)
                {
                    final Projectile aoe = createProjectile(1);
                    aoe.setPos(Projectile.getX(), Projectile.getY(), Projectile.getZ());
                    doRangedAttack(aoe, Player);
                    if (Player == target)
                    {
                        containedTarget = true;
                    }
                }

                if (!containedTarget)
                {
                    final Projectile aoe = createProjectile(1);
                    aoe.setPos(Projectile.getX(), Projectile.getY(), Projectile.getZ());
                    doRangedAttack(aoe, target);
                }
            }
            else
            {
                doRangedAttack(Projectile, target);
            }

            if (!projectileEntities.isEmpty())
            {
                attackTime = 10;
                return;
            }

            relativeAttackDist = (float) (Math.sqrt(distSqr) / this.params.distance);
            this.attackTime = Mth.floor(
              (1 + BrutalBosses.rand.nextFloat() * 0.5f) * (relativeAttackDist * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin))
                                - 5 * params.count;
        }
        else if (this.attackTime < 0)
        {
            relativeAttackDist = (float) (Math.sqrt(distSqr) / this.params.distance);
            this.attackTime = Mth.floor(
              (1 + BrutalBosses.rand.nextFloat() * 0.5f) * (relativeAttackDist * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin))
                                - Math.min(5 * params.count, attackIntervalMin - 5);
        }
    }

    /**
     * Creator for a fake projectile entity
     *
     * @return
     */
    protected abstract Projectile createProjectile();

    protected abstract void doRangedAttack(final Projectile Projectile, final LivingEntity target);

    /**
     * Creates a projectile for the given number and positions it
     *
     * @param number
     * @return
     */
    protected Projectile createProjectile(int number)
    {
        final Projectile Projectile = createProjectile();

        positionProjectile(Projectile, number);
        Projectile.noPhysics = true;
        Projectile.setNoGravity(true);
        Projectile.setOwner(mob);
        if (Projectile instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) Projectile).setMaxLifeTime(mob.level.getGameTime() + 20 * 60);
        }

        mob.level.addFreshEntity(Projectile);
        return Projectile;
    }

    /**
     * Positions the projectiles in a circle around the entity
     *
     * @param Projectile
     * @param number
     */
    protected void positionProjectile(final Projectile Projectile, final int number)
    {
        if (params.count == 1)
        {
            final Direction dir = PosUtil.getFacing(mob.position(), target.position()).getClockWise();
            Projectile.setPos(mob.getX() + dir.getCounterClockWise().getStepX() * 0.5,
              mob.getY() + mob.getEyeHeight() - 0.5,
              mob.getZ() + dir.getCounterClockWise().getStepZ() * 0.5);
            return;
        }

        final Direction dir = PosUtil.getFacing(mob.position(), target.position()).getClockWise();

        Vec3 center = new Vec3(mob.getX(), mob.getY() - 0.5 + mob.getEyeHeight() * cap.getBossType().getVisualScale(), mob.getZ());

        double y = 0.75d * Math.cos(Math.toRadians((360d / (params.count + 1)) * (number)));
        double xzRatio = 0.75d * Math.sin(Math.toRadians((360d / (params.count + 1)) * (number)));

        Vec3 offSet = new Vec3(xzRatio * dir.getStepX(), -y, xzRatio * dir.getStepZ());

        center.add(offSet);
        center.add(new Vec3(dir.getCounterClockWise().getStepX() * 0.5, 0, dir.getCounterClockWise().getStepZ() * 0.5));
        Projectile.setPos(center.x, center.y, center.z);
    }

    public static class RangedParams extends IAIParams.DefaultParams
    {
        protected int     count    = 1;
        protected int     interval = 50;
        protected int     distance = 15;
        protected boolean aoe      = false;
        protected float   speed    = 1.0f;

        public RangedParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String PROJECTILE_COUNT    = "projectile_count";
        private static final String PROJECTILE_INTERVAL = "projectile_interval";
        private static final String PROJECTILE_DISTANCE = "projectile_distance";
        private static final String PROJECTILE_AOE      = "projectile_aoe";
        private static final String PROJECTILE_SPEED    = "projectile_speed";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(PROJECTILE_COUNT))
            {
                count = jsonElement.get(PROJECTILE_COUNT).getAsInt();
            }

            if (jsonElement.has(PROJECTILE_INTERVAL))
            {
                interval = jsonElement.get(PROJECTILE_INTERVAL).getAsInt();
            }

            if (jsonElement.has(PROJECTILE_DISTANCE))
            {
                distance = jsonElement.get(PROJECTILE_DISTANCE).getAsInt();
            }
            if (jsonElement.has(PROJECTILE_AOE))
            {
                aoe = jsonElement.get(PROJECTILE_AOE).getAsBoolean();
            }
            if (jsonElement.has(PROJECTILE_SPEED))
            {
                speed = jsonElement.get(PROJECTILE_SPEED).getAsFloat();
            }

            return this;
        }
    }
}
