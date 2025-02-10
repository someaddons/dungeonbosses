package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.data.PotionData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Occasionally charge at an entity
 */
public class JumpAttackGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:jumpattack");

    private final Mob              mob;
    private       JumpAttackParams params;
    private       LivingEntity     target        = null;
    private       Vec3             end;
    private       int              animationTick = 0;
    private       double           distance      = 10;

    public JumpAttackGoal(Mob mob, final IAIParams params)
    {
        this.params = (JumpAttackParams) params;
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
            mob.setShiftKeyDown(true);
            return;
        }

        double dist = Math.sqrt(this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ()));

        // add param maxdist
        if (dist >= params.minDistance && dist <= params.maxDistance && !isCharging && mob.getSensing().hasLineOfSight(target)
            && Math.abs(mob.getBlockY() - target.getBlockY()) < params.maxJumpHeight)
        {
            final Path path = mob.getNavigation().getPath();
            if (path != null)
            {
                isCharging = true;
                end = target.position();
                distance = mob.position().distanceTo(end);
                animationTick = 0;

                mob.level().playSound(null,
                    mob.getX(),
                    mob.getY(),
                    mob.getZ(),
                    SoundEvents.FIRECHARGE_USE,
                    mob.getSoundSource(),
                    2.0F,
                    1.0F);

                mob.level().playSound(null,
                    mob.getX(),
                    mob.getY(),
                    mob.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    mob.getSoundSource(),
                    1.5F,
                    1.0F);

                mob.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 1));

                double d0 = (double) (-Mth.sin(mob.getYRot() * ((float) Math.PI / 180)));
                double d1 = (double) Mth.cos(mob.getYRot() * ((float) Math.PI / 180));
                if (mob.level() instanceof ServerLevel)
                {
                    ((ServerLevel) mob.level()).sendParticles(ParticleTypes.POOF,
                        mob.getX() + d0,
                        mob.getY(0.5D),
                        mob.getZ() + d1,
                        30,
                        d0,
                        0.0D,
                        d1,
                        0.0D);

                    ((ServerLevel) mob.level()).sendParticles(ParticleTypes.GLOW,
                        mob.getX() + d0,
                        mob.getY(0.5D),
                        mob.getZ() + d1,
                        500,
                        d0,
                        0.1D,
                        d1,
                        10.0D);
                }
            }
        }

        if (!isCharging)
        {
            return;
        }

        params.duration = 20;
        mob.getLookControl().setLookAt(target);
        mob.getLookControl().tick();
        if (animationTick <= params.duration * 0.1)
        {
            // Move faster
            final double distanceModifier = distance / params.duration;
            final Vec3 direction = end.subtract(mob.position()).multiply(1, 0, 1).normalize().multiply(distanceModifier, 1, distanceModifier);
            mob.setDeltaMovement(direction);
            mob.setShiftKeyDown(true);
        }
        else if (animationTick <= (params.duration * 0.1) + 4)
        {
            // jump
            mob.setJumping(true);
            mob.setShiftKeyDown(false);
            mob.getJumpControl().jump();
            final double distanceModifier = distance / params.duration;
            final Vec3 direction = end.subtract(mob.position()).multiply(1, 0, 1).normalize().add(0, 0.75, 0).multiply(distanceModifier, distanceModifier, distanceModifier);
            mob.setDeltaMovement(direction);
        }
        else if (animationTick <= params.duration && mob.position().distanceTo(end) >= 2)
        {
            final double distanceModifier = distance / params.duration;
            final Vec3 direction = end.subtract(mob.position()).multiply(1, 1, 1).normalize().multiply(distanceModifier, distanceModifier, distanceModifier);
            mob.setDeltaMovement(direction);
        }

        if ((mob.onGround() && mob.position().distanceTo(end) < 2) || animationTick == params.duration)
        {
            // Arrival
            isCharging = false;

            // Cooldown
            ticksToNextUpdate = (int) (params.interval + (BrutalBosses.rand.nextInt(20) - 10));
            ticksToNextUpdate = 100;
            // Stop movement for 1sec
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3));
            mob.setDeltaMovement(Vec3.ZERO.add(0, -0.8, 0));

            // Arrival ssfx
            mob.level().playSound(null,
                mob.getX(),
                mob.getY(),
                mob.getZ(),
                SoundEvents.ANVIL_LAND,
                mob.getSoundSource(),
                100.0F,
                0.1F);
            mob.level().playSound(null,
                mob.getX(),
                mob.getY(),
                mob.getZ(),
                SoundEvents.IRON_GOLEM_DAMAGE,
                mob.getSoundSource(),
                100.0F,
                0.1F);
            double d0 = 3;
            double d1 = 3;
            if (mob.level() instanceof ServerLevel)
            {
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.LARGE_SMOKE,
                    mob.getX() + d0,
                    mob.getY(),
                    mob.getZ() + d1,
                    300,
                    d0,
                    0.0D,
                    d1,
                    0.0D);
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.SMOKE,
                    mob.getX() + d0,
                    mob.getY(),
                    mob.getZ() + d1,
                    300,
                    d0,
                    0.0D,
                    d1,
                    0.0D);
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.FIREWORK,
                    mob.getX() + d0,
                    mob.getY(),
                    mob.getZ() + d1,
                    300,
                    d0,
                    0.0D,
                    d1,
                    0.0D);
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    mob.getX() + d0,
                    mob.getY(),
                    mob.getZ() + d1,
                    300,
                    d0,
                    0.0D,
                    d1,
                    0.0D);
            }

            // Arrival attack
            if (mob.distanceTo(target) <= params.attackDistance)
            {
                this.mob.swing(InteractionHand.MAIN_HAND);
                float damage = params.extraDamage;
                if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
                {
                    damage += mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                }
                target.hurt(mob.damageSources().mobAttack(mob), damage);
            }

            // Arrival Lingering potion cloud
            for (final PotionData potionData : params.potions)
            {
                AreaEffectCloud areaeffectcloudentity =
                    new AreaEffectCloud(mob.level(), mob.getX(), mob.getY(), mob.getZ());
                areaeffectcloudentity.setOwner(mob);

                // Spawn ground area effect making the player walk away
                areaeffectcloudentity.setParticle(potionData.particleType() instanceof ParticleOptions
                    ? (ParticleOptions) potionData.particleType()
                    : ParticleTypes.SOUL_FIRE_FLAME);
                areaeffectcloudentity.setRadius(params.potionRadius);
                areaeffectcloudentity.setDuration(10 * 20);
                areaeffectcloudentity.addEffect(new MobEffectInstance(potionData.effect(), potionData.duration(), potionData.amplifier()));
                mob.level().addFreshEntity(areaeffectcloudentity);
            }
        }
        else
        {
            // During jump sfx
            if (mob.level() instanceof ServerLevel)
            {
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.CLOUD,
                    mob.getX(),
                    mob.getY(0.5D),
                    mob.getZ(),
                    10,
                    0.1,
                    0.0D,
                    0.1,
                    0.05D);
            }
        }


        animationTick++;
    }

    public static class JumpAttackParams extends IAIParams.DefaultParams
    {
        private float            maxDistance    = 15;
        private float            minDistance    = 3f;
        private float            maxJumpHeight  = 4;
        private int              duration       = 10;
        private float            interval       = 200;
        private float            extraDamage    = 2f;
        private float            attackDistance = 4f;
        private List<PotionData> potions        = new ArrayList<>();
        private int              potionRadius   = 5;

        public JumpAttackParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String ATKDIST   = "attackdist";
        private static final String MAXHEIGHT = "maxheight";
        private static final String EXTDMG    = "extradamage";
        public static final  String MINDIST   = "mindist";
        public static final  String MAXDIST   = "maxdist";
        public static final  String DURATION  = "duration";
        public static final  String COOLDOWN  = "interval";

        public static final String POTIONS         = "potions";
        public static final String POTIONRADIUS    = "potionradius";
        public static final String POTIONDURATION  = "duration";
        public static final String POTIONAMPLIFIER = "amplifier";
        public static final String POTIONPARTICLE  = "particle";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(ATKDIST))
            {
                attackDistance = jsonElement.get(ATKDIST).getAsFloat();
            }

            if (jsonElement.has(EXTDMG))
            {
                extraDamage = jsonElement.get(EXTDMG).getAsFloat();
            }

            if (jsonElement.has(MINDIST))
            {
                minDistance = jsonElement.get(MINDIST).getAsFloat();
            }

            if (jsonElement.has(MAXDIST))
            {
                maxDistance = jsonElement.get(MAXDIST).getAsFloat();
            }

            if (jsonElement.has(DURATION))
            {
                duration = jsonElement.get(DURATION).getAsInt();
            }

            if (jsonElement.has(POTIONRADIUS))
            {
                potionRadius = jsonElement.get(POTIONRADIUS).getAsInt();
            }

            if (jsonElement.has(COOLDOWN))
            {
                interval = jsonElement.get(COOLDOWN).getAsFloat();
            }

            if (jsonElement.has(MAXHEIGHT))
            {
                maxJumpHeight = jsonElement.get(MAXHEIGHT).getAsFloat();
            }

            if (jsonElement.has(POTIONS))
            {
                potions = new ArrayList<>();
                for (Map.Entry<String, JsonElement> data : jsonElement.get(POTIONS).getAsJsonObject().entrySet())
                {
                    JsonObject potionData = data.getValue().getAsJsonObject();

                    ParticleType particleType = null;
                    if (potionData.has(POTIONPARTICLE))
                    {
                        particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.tryParse(potionData.get(POTIONPARTICLE).getAsString()));
                    }

                    potions.add(new PotionData(BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.tryParse(data.getKey())),
                        potionData.get(POTIONDURATION).getAsInt(),
                        potionData.get(POTIONAMPLIFIER).getAsInt(),
                        particleType));
                }
            }

            return this;
        }
    }
}