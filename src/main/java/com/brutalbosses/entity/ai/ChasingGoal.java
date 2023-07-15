package com.brutalbosses.entity.ai;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

/**
 * Simply chases the target at the required distance
 */
public class ChasingGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:chasetarget");

    private final Mob          mob;
    private       float        chaseDist;
    private       LivingEntity target = null;
    private final ChaseParams  params;

    public ChasingGoal(Mob mob, final IAIParams params)
    {
        this.params = (ChaseParams) params;
        chaseDist = this.params.chasedistance * this.params.chasedistance;
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            this.target = target;
            return params.healthPhaseCheck.test(mob);
        }
        else
        {
            return false;
        }
    }

    public void stop()
    {
        this.target = null;
    }

    private int ticksToNextUpdate = 0;

    public void tick()
    {
        mob.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
        if (--ticksToNextUpdate > 0)
        {
            return;
        }

        double distSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());

        ticksToNextUpdate = (int) Math.max(4, (distSqr / 100));

        if (distSqr <= (double) this.chaseDist && this.mob.getSensing().hasLineOfSight(this.target))
        {
            this.mob.getNavigation().stop();
        }
        else
        {
            final Path path = mob.getNavigation().getPath();
            if (path != null)
            {
                final Node endNode = path.getEndNode();
                if (endNode != null)
                {
                    final BlockPos endPos = new BlockPos(endNode.x, endNode.y, endNode.z);
                    final double endPosDist = this.target.distanceToSqr(endPos.getX(), endPos.getY(), endPos.getZ());
                    if (endPosDist > chaseDist || !this.mob.getSensing().hasLineOfSight(this.target))
                    {
                        this.mob.getNavigation().moveTo(this.target, 1.0f);
                    }
                }
            }
            else
            {
                this.mob.getNavigation().moveTo(this.target, 1.0f);
            }
        }
    }

    public static class ChaseParams extends IAIParams.DefaultParams
    {
        private float chasedistance = 2f;

        public ChaseParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String CHASE_DIST = "chasedistance";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);
            if (jsonElement.has(CHASE_DIST))
            {
                chasedistance = jsonElement.get(CHASE_DIST).getAsFloat();
            }
            return this;
        }
    }
}