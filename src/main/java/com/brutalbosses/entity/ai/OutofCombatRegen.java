package com.brutalbosses.entity.ai;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Simply chases the target at the required distance
 */
public class OutofCombatRegen extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:aftercombatregen");

    private final Mob   mob;
    private final float amount;
    private       int       combatTimer = 0;

    public OutofCombatRegen(Mob mob, final IAIParams params)
    {
        amount = ((CombatParams) params).amount;
        this.mob = mob;
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            combatTimer = 20 * 30;
        }
        else if (combatTimer > 0)
        {
            combatTimer--;
        }
        else
        {
            combatTimer = 20;
            mob.heal(amount);
        }

        return false;
    }

    public void stop()
    {
    }

    public void tick()
    {

    }

    public static class CombatParams extends IAIParams.DefaultParams
    {
        private float amount = 2.0f;

        public CombatParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String AMOUNT = "amount";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);
            if (jsonElement.has(AMOUNT))
            {
                amount = jsonElement.get(AMOUNT).getAsFloat();
            }
            return this;
        }
    }
}