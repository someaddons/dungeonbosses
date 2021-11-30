package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Simple whirldwind attack, can apply potion effects here!
 */
public class WhirldwindMelee extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:whirldwind");

    private final MobEntity        mob;
    private       LivingEntity     target = null;
    private       WhirldWindParams params;

    public WhirldwindMelee(MobEntity mob)
    {
        final BossCapability cap = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
        params = ((WhirldWindParams) cap.getBossType().getAIParams(ID));
        this.mob = mob;
    }

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
            final List<LivingEntity> entities = mob.level.getLoadedEntitiesOfClass(PlayerEntity.class, mob.getBoundingBox().inflate(2.0D, 0.5D, 2.0D));
            if (!entities.contains(mob.getTarget()))
            {
                entities.add(mob.getTarget());
            }

            for (LivingEntity livingentity : entities)
            {
                if (livingentity != mob)
                {
                    livingentity.knockback(
                      params.knockback,
                      MathHelper.sin(livingentity.yRot * ((float) Math.PI)),
                      (-MathHelper.cos(livingentity.yRot * ((float) Math.PI))));
                    this.mob.swing(Hand.MAIN_HAND);

                    float damage = params.extraDamage;
                    if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
                    {
                        damage += mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    }
                    livingentity.hurt(DamageSource.mobAttack(mob), damage);

                    if (params.onHitEffect != null)
                    {
                        livingentity.addEffect(new EffectInstance(params.onHitEffect, params.potionduration, params.potionlevel));
                    }
                }
            }

            mob.level.playSound(null,
              mob.getX(),
              mob.getY(),
              mob.getZ(),
              SoundEvents.PLAYER_ATTACK_SWEEP,
              mob.getSoundSource(),
              1.0F,
              1.0F);

            double d0 = (double) (-MathHelper.sin(mob.yRot * ((float) Math.PI / 180)));
            double d1 = (double) MathHelper.cos(mob.yRot * ((float) Math.PI / 180));
            if (mob.level instanceof ServerWorld)
            {
                ((ServerWorld) mob.level).sendParticles(ParticleTypes.SWEEP_ATTACK,
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

    public static final String ATKDIST    = "attackdist";
    public static final String EXTDMG     = "extradamage";
    public static final String POTION     = "potiononhit";
    public static final String POTION_STR = "potionlevel";
    public static final String POTION_DUR = "potionduration";
    public static final String KNOCK      = "knockback";
    public static final String COOLDOWN   = "cooldown";

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    public static IAIParams parse(final JsonObject jsonElement)
    {
        final WhirldWindParams params = new WhirldWindParams();
        if (jsonElement.has(ATKDIST))
        {
            params.attackDistance = jsonElement.get(ATKDIST).getAsFloat();
            params.attackDistance *= params.attackDistance;
        }

        if (jsonElement.has(EXTDMG))
        {
            params.extraDamage = jsonElement.get(EXTDMG).getAsFloat();
        }

        if (jsonElement.has(POTION_STR))
        {
            params.potionlevel = jsonElement.get(POTION_STR).getAsInt();
        }

        if (jsonElement.has(POTION_DUR))
        {
            params.potionduration = jsonElement.get(POTION_DUR).getAsInt();
        }

        if (jsonElement.has(COOLDOWN))
        {
            params.cooldown = jsonElement.get(COOLDOWN).getAsInt();
        }

        if (jsonElement.has(KNOCK))
        {
            params.knockback = jsonElement.get(KNOCK).getAsFloat();
        }

        if (jsonElement.has(POTION))
        {
            final ResourceLocation effectID = new ResourceLocation(jsonElement.get(POTION).getAsString());
            params.onHitEffect = ForgeRegistries.POTIONS.getValue(effectID);
        }

        return params;
    }

    private static class WhirldWindParams implements IAIParams
    {
        private float  attackDistance = 4f;
        private float  extraDamage    = 2f;
        private Effect onHitEffect    = null;
        private float  knockback      = 4f;
        private int    cooldown       = 80;
        private int    potionlevel    = 1;
        private int    potionduration = 60;

        private WhirldWindParams()
        {
        }
    }
}