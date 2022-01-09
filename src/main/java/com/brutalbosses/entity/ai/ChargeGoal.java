package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

/**
 * Occasionally charge at an entity
 */
public class ChargeGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:charge");

    private static final AttributeModifier speedMod = new AttributeModifier("brutalbosses:speedbuff", 2, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private final MobEntity    mob;
    private       ChargeParams params;
    private       LivingEntity target = null;

    public ChargeGoal(MobEntity mob, final IAIParams params)
    {
        this.params = (ChargeParams) params;
        this.mob = mob;
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

    private int     ticksToNextUpdate = 0;
    private boolean isCharging        = false;

    public void tick()
    {
        if (--ticksToNextUpdate > 0)
        {
            return;
        }

        double distSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());

        ticksToNextUpdate = (int) Math.max(4, (distSqr / 100));

        if (distSqr >= params.minDistance && !isCharging)
        {
            final Path path = mob.getNavigation().getPath();
            if (path != null)
            {
                isCharging = true;
                ticksToNextUpdate = (int) params.duration;

                mob.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(speedMod);
                mob.setSpeed((float) mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                // Start dust particles

                mob.level.playSound(null,
                  mob.getX(),
                  mob.getY(),
                  mob.getZ(),
                  SoundEvents.CAT_HISS,
                  mob.getSoundSource(),
                  1.0F,
                  1.0F);

                double d0 = (double) (-MathHelper.sin(mob.yRot * ((float) Math.PI / 180)));
                double d1 = (double) MathHelper.cos(mob.yRot * ((float) Math.PI / 180));
                if (mob.level instanceof ServerWorld)
                {
                    ((ServerWorld) mob.level).sendParticles(ParticleTypes.CLOUD,
                      mob.getX() + d0,
                      mob.getY(0.5D),
                      mob.getZ() + d1,
                      20,
                      d0,
                      0.0D,
                      d1,
                      0.0D);
                }

                return;
            }
        }

        if (isCharging)
        {
            isCharging = false;
            ticksToNextUpdate = (int) (params.interval + (BrutalBosses.rand.nextInt(20) - 10));
            mob.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(speedMod);
            mob.setSpeed((float) mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
            // stop dust particles
            mob.level.playSound(null,
              mob.getX(),
              mob.getY(),
              mob.getZ(),
              SoundEvents.CAT_HISS,
              mob.getSoundSource(),
              1.0F,
              1.0F);

            double d0 = (double) (-MathHelper.sin(mob.yRot * ((float) Math.PI / 180)));
            double d1 = (double) MathHelper.cos(mob.yRot * ((float) Math.PI / 180));
            if (mob.level instanceof ServerWorld)
            {
                ((ServerWorld) mob.level).sendParticles(ParticleTypes.CLOUD,
                  mob.getX() + d0,
                  mob.getY(0.5D),
                  mob.getZ() + d1,
                  20,
                  d0,
                  0.0D,
                  d1,
                  0.0D);
            }
        }
    }

    public static class ChargeParams extends IAIParams.DefaultParams
    {
        private float minDistance = 3f;
        private float duration    = 20;
        private float interval    = 200;

        public ChargeParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        public static final String CHARGE_MINDIST  = "mindist";
        public static final String CHARGE_DURATION = "duration";
        public static final String CHARGE_COOLDOWN = "interval";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(CHARGE_MINDIST))
            {
                minDistance = jsonElement.get(CHARGE_MINDIST).getAsFloat();
            }

            if (jsonElement.has(CHARGE_DURATION))
            {
                duration = jsonElement.get(CHARGE_DURATION).getAsFloat();
            }

            if (jsonElement.has(CHARGE_COOLDOWN))
            {
                interval = jsonElement.get(CHARGE_COOLDOWN).getAsFloat();
            }

            return this;
        }
    }
}