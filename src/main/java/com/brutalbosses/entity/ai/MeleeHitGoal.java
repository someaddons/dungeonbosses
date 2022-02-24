package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Simple melee hit goal, simply hits its target
 */
public class MeleeHitGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:meleehit");

    private final Mob            mob;
    private       LivingEntity   target = null;
    private       MeleeHitParams params;

    public MeleeHitGoal(Mob mob, final IAIParams params)
    {
        this.params = (MeleeHitParams) params;
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

        if (distSqr < params.attackDistance * params.attackDistance)
        {
            attackTimer = (int) (params.cooldown * (BrutalBosses.rand.nextFloat() * 0.5 + 0.75f));

            this.mob.swing(InteractionHand.MAIN_HAND);

            float damage = params.extraDamage;
            if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
            {
                damage += mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
            }
            damage += EnchantmentHelper.getDamageBonus(mob.getMainHandItem(), target.getMobType());

            if (target.hurt(DamageSource.mobAttack(mob), damage))
            {
                int fireAspect = EnchantmentHelper.getFireAspect(mob);
                if (fireAspect > 0)
                {
                    target.setSecondsOnFire(fireAspect * 4);
                }
                if (params.onHitMobEffect != null)
                {
                    target.addEffect(new MobEffectInstance(params.onHitMobEffect, params.potionduration, params.potionlevel));
                }

                float knockBack = params.knockback;
                if (mob.getAttributes().hasAttribute(Attributes.ATTACK_KNOCKBACK))
                {
                    knockBack += (float) mob.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                }
                knockBack += (float) EnchantmentHelper.getKnockbackBonus(mob);
                if (knockBack > 0.0F)
                {
                    target.knockback(knockBack * 0.5F, Mth.sin(mob.getYRot() * ((float) Math.PI / 180F)), (-Mth.cos(mob.getYRot() * ((float) Math.PI / 180F))));
                }

                mob.doEnchantDamageEffects(mob, target);
                mob.setLastHurtMob(target);
            }
        }
    }

    public static class MeleeHitParams extends IAIParams.DefaultParams
    {
        private float     attackDistance = 2f;
        private float     extraDamage    = 2f;
        private MobEffect onHitMobEffect = null;
        private float     knockback      = 0f;
        private int       cooldown       = 30;
        private int       potionlevel    = 1;
        private int       potionduration = 60;

        public MeleeHitParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String ATKDIST    = "attackdist";
        private static final String EXTDMG     = "damage";
        private static final String POTION     = "potiononhit";
        private static final String POTION_STR = "potionlevel";
        private static final String POTION_DUR = "potionduration";
        private static final String KNOCK      = "knockback";
        private static final String COOLDOWN   = "cooldown";

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

            if (jsonElement.has(POTION))
            {
                final ResourceLocation MobEffectID = new ResourceLocation(jsonElement.get(POTION).getAsString());
                onHitMobEffect = ForgeRegistries.MOB_EFFECTS.getValue(MobEffectID);
            }
            return this;
        }
    }
}