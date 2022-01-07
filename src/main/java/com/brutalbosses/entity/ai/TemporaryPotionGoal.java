package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies a temporary potion and displays a banner to give a hint at its effects
 */
public class TemporaryPotionGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:temppotions");

    private final MobEntity        mob;
    private       TempPotionParams params;
    private       LivingEntity     target = null;

    public TemporaryPotionGoal(MobEntity mob)
    {
        final BossCapability cap = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
        params = ((TempPotionParams) cap.getBossType().getAIParams(ID));
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
        for (final Tuple<Effect, Integer> potion : params.potions)
        {
            final EffectInstance effectInstance = mob.getEffect(potion.getA());
            if (effectInstance != null && effectInstance.getDuration() < params.duration)
            {
                mob.removeEffect(potion.getA());
            }
        }
    }

    private int ticksToNextUpdate = 0;

    public void tick()
    {
        if (--ticksToNextUpdate > 0)
        {
            return;
        }

        ticksToNextUpdate = (int) params.interval;

        for (final Tuple<Effect, Integer> potion : params.potions)
        {
            mob.addEffect(new EffectInstance(potion.getA(), params.duration, potion.getB()));
        }

        if (params.item != null)
        {
            final ThrownItemEntity item = ModEntities.THROWN_ITEMC.create(mob.level);
            item.setPos(mob.getX(), mob.getY(), mob.getZ());
            mob.level.addFreshEntity(item);
            item.startRiding(mob, true);
            ((IOnProjectileHit) item).setMaxLifeTime(mob.level.getGameTime() + params.duration);
            item.setItem(params.item);
            item.setScale(params.visibleitemsize);
        }

        mob.level.playSound(null,
          mob.getX(),
          mob.getY(),
          mob.getZ(),
          SoundEvents.WANDERING_TRADER_DRINK_POTION,
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

    public static class TempPotionParams extends IAIParams.DefaultParams
    {
        private int                          duration        = 100;
        private float                        interval        = 200;
        private List<Tuple<Effect, Integer>> potions         = new ArrayList<>();
        private ItemStack                    item            = null;
        private float                        visibleitemsize = 2.0f;

        public TempPotionParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        public static final String POTION_DURATION = "duration";
        public static final String COOLDOWN        = "interval";
        public static final String POTIONS         = "potions";
        public static final String ITEM            = "visibleitem";
        public static final String ITEMSIZE        = "visibleitemsize";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(POTION_DURATION))
            {
                duration = jsonElement.get(POTION_DURATION).getAsInt();
            }

            if (jsonElement.has(COOLDOWN))
            {
                interval = jsonElement.get(COOLDOWN).getAsFloat();
            }

            if (jsonElement.has(ITEMSIZE))
            {
                visibleitemsize = jsonElement.get(ITEMSIZE).getAsFloat();
            }

            if (jsonElement.has(ITEM))
            {
                try
                {
                    item = ItemStack.of(JsonToNBT.parseTag(jsonElement.get(ITEM).getAsString()));
                }
                catch (CommandSyntaxException e)
                {
                    BrutalBosses.LOGGER.warn("Could not parse item of: " + jsonElement.get(ITEM).getAsString(), e);
                    throw new UnsupportedOperationException();
                }
            }

            if (jsonElement.has(POTIONS))
            {
                potions = new ArrayList<>();
                for (Map.Entry<String, JsonElement> data : jsonElement.get(POTIONS).getAsJsonObject().entrySet())
                {
                    potions.add(new Tuple<>(ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(data.getKey())), data.getValue().getAsInt()));
                }
            }

            return this;
        }
    }
}