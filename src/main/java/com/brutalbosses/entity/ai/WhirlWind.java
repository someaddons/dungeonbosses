package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.google.gson.JsonObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple whirldwind attack, can apply potion MobEffects here!
 */
public class WhirlWind extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:whirlwind");

    private final Mob              mob;
    private       LivingEntity     target = null;
    private       WhirldWindParams params;

    public WhirlWind(Mob mob, final IAIParams params)
    {
        this.params = (WhirldWindParams) params;
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
        attackTimer = 0;
    }

    private int attackTimer = 0;

    public void tick()
    {
        if (--attackTimer > 0)
        {
            return;
        }

        double distSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());

        if (distSqr < params.attackDistance * params.attackDistance && BrutalBosses.rand.nextInt(40) == 0)
        {
            attackTimer = params.cooldown;
            List<LivingEntity> entities = new ArrayList<>(mob.level().getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(2.0D, 0.5D, 2.0D)));
            if (!entities.contains(target))
            {
                entities.add(target);
            }

            for (LivingEntity livingentity : entities)
            {
                if (livingentity != mob && livingentity != null && livingentity.isAlive())
                {
                    if (params.knockup)
                    {
                        livingentity.setDeltaMovement(livingentity.getDeltaMovement().add(0.0D, params.knockback / 5f, 0.0D));
                    }
                    else
                    {
                        livingentity.knockback(
                          params.knockback,
                          Mth.sin(livingentity.getYRot() * ((float) Math.PI)),
                          (-Mth.cos(livingentity.getYRot() * ((float) Math.PI))));
                    }

                    this.mob.swing(InteractionHand.MAIN_HAND);
                    float damage = params.extraDamage;
                    if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
                    {
                        damage += mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    }
                    livingentity.hurt(mob.damageSources().mobAttack(mob), damage);

                    if (params.onHitMobEffect != null)
                    {
                        livingentity.addEffect(new MobEffectInstance(params.onHitMobEffect, params.potionduration, params.potionlevel));
                    }
                }
            }

            mob.level().playSound(null,
              mob.getX(),
              mob.getY(),
              mob.getZ(),
              SoundEvents.PLAYER_ATTACK_SWEEP,
              mob.getSoundSource(),
              1.0F,
              1.0F);

            double d0 = (double) (-Mth.sin(mob.getYRot() * ((float) Math.PI / 180)));
            double d1 = (double) Mth.cos(mob.getYRot() * ((float) Math.PI / 180));
            if (mob.level() instanceof ServerLevel)
            {
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.SWEEP_ATTACK,
                  mob.getX() + d0,
                  mob.getY(0.5D),
                  mob.getZ() + d1,
                  2,
                  d0,
                  0.0D,
                  d1,
                  0.0D);
            }
        }
    }

    public static class WhirldWindParams extends IAIParams.DefaultParams
    {
        private float     attackDistance = 4f;
        private float     extraDamage    = 2f;
        private MobEffect onHitMobEffect = null;
        private float     knockback      = 4f;
        private int       cooldown       = 80;
        private int       potionlevel    = 1;
        private int       potionduration = 60;
        private boolean   knockup        = false;

        public WhirldWindParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String ATKDIST    = "attackdist";
        private static final String EXTDMG     = "extradamage";
        private static final String POTION     = "potiononhit";
        private static final String POTION_STR = "potionlevel";
        private static final String POTION_DUR = "potionduration";
        private static final String KNOCK      = "knockback";
        private static final String COOLDOWN   = "cooldown";
        private static final String KNOCK_UP   = "knockup";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);
            if (jsonElement.has(ATKDIST))
            {
                attackDistance = jsonElement.get(ATKDIST).getAsFloat();
                attackDistance *= attackDistance;
            }

            if (jsonElement.has(EXTDMG))
            {
                extraDamage = jsonElement.get(EXTDMG).getAsFloat();
            }

            if (jsonElement.has(POTION_STR))
            {
                potionlevel = jsonElement.get(POTION_STR).getAsInt();
            }

            if (jsonElement.has(POTION_DUR))
            {
                potionduration = jsonElement.get(POTION_DUR).getAsInt();
            }

            if (jsonElement.has(COOLDOWN))
            {
                cooldown = jsonElement.get(COOLDOWN).getAsInt();
            }

            if (jsonElement.has(KNOCK))
            {
                knockback = jsonElement.get(KNOCK).getAsFloat();
            }

            if (jsonElement.has(KNOCK_UP))
            {
                knockup = jsonElement.get(KNOCK_UP).getAsBoolean();
            }

            if (jsonElement.has(POTION))
            {
                final ResourceLocation MobEffectID = new ResourceLocation(jsonElement.get(POTION).getAsString());
                onHitMobEffect = ForgeRegistries.MOB_EFFECTS.getValue(MobEffectID);
            }

            return this;
        }
    }
}