package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.capability.BossCapEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

import static com.brutalbosses.entity.CustomAttributes.ATTACK_SPEED;

/**
 * Attack goal with shield usage and proper reset timer
 */
public class MeleeShieldAttackGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:shieldmelee");

    public MeleeShieldAttackGoal(Mob entity, double speed)
    {
        this.mob = entity;
        this.speedModifier = speed;
        this.followingTargetEvenIfNotSeen = true;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        bossType = ((BossCapEntity)mob).getBossCap().getBossType();
        attackInterval = bossType.getCustomAttributeValueOrDefault(ATTACK_SPEED, 1);
    }

    private final   BossType bossType;
    protected final Mob      mob;
    private final   double   speedModifier;
    private final   boolean  followingTargetEvenIfNotSeen;
    private         Path     path;
    private         double   pathedTargetX;
    private         double   pathedTargetY;
    private         double   pathedTargetZ;
    private         int      ticksUntilNextPathRecalculation;
    private         int      ticksUntilNextAttack;
    private final   double   attackInterval;
    private         long     lastCanUseCheck;

    public boolean canUse()
    {
        long timeDiff = this.mob.level().getGameTime();
        if (timeDiff - this.lastCanUseCheck < 20L)
        {
            return false;
        }
        else
        {
            this.lastCanUseCheck = timeDiff;
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null || !livingentity.isAlive())
            {
                return false;
            }

            this.path = this.mob.getNavigation().createPath(livingentity, 0);
            if (this.path != null)
            {
                return true;
            }
            else
            {
                return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            }
        }
    }

    public boolean canContinueToUse()
    {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null)
        {
            return false;
        }
        else if (!livingentity.isAlive())
        {
            return false;
        }
        else if (!this.followingTargetEvenIfNotSeen)
        {
            return !this.mob.getNavigation().isDone();
        }
        else if (!this.mob.isWithinRestriction(livingentity.blockPosition()))
        {
            return false;
        }
        else
        {
            return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity).isCreative();
        }
    }

    public void start()
    {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    public void stop()
    {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        this.mob.startUsingItem(InteractionHand.MAIN_HAND);
    }

    public void tick()
    {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null)
        {
            return;
        }

        this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
        double d0 = this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (
          this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D
            || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.mob.getRandom().nextFloat() < 0.05F))
        {
            this.pathedTargetX = livingentity.getX();
            this.pathedTargetY = livingentity.getY();
            this.pathedTargetZ = livingentity.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);

            if (d0 > 1024.0D)
            {
                this.ticksUntilNextPathRecalculation += 10;
            }
            else if (d0 > 256.0D)
            {
                this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier))
            {
                this.ticksUntilNextPathRecalculation += 15;
            }
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        this.checkAndPerformAttack(livingentity, d0);
    }

    int shieldTicks = 0;

    protected void checkAndPerformAttack(LivingEntity target, double dist)
    {
        double d0 = this.getAttackReachSqr(target);
        if (dist <= d0 && this.ticksUntilNextAttack <= 0)
        {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }

        if (shieldTicks == -20 && BrutalBosses.rand.nextInt(20) == 0)
        {
            this.mob.startUsingItem(InteractionHand.OFF_HAND);
            shieldTicks = BrutalBosses.rand.nextInt(10) + 20;
        }

        if (shieldTicks > -20)
        {
            shieldTicks--;
            if (shieldTicks == 0)
            {
                mob.stopUsingItem();
            }
        }
    }

    protected void resetAttackCooldown()
    {
        this.ticksUntilNextAttack = (int) (20 / attackInterval);
    }

    protected double getAttackReachSqr(LivingEntity entity)
    {
        return (double) (this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F) * bossType.getVisualScale();
    }
}
