package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Type for holding AI param classes
 */
public interface IAIParams
{
    /**
     * Empty paramts instance
     */
    public static DefaultParams EMPTY = new DefaultParams();

    IAIParams parse(final JsonObject jsonElement);

    /**
     * Default params
     */
    public static class DefaultParams implements IAIParams
    {
        public Predicate<LivingEntity> healthPhaseCheck = e -> true;

        public DefaultParams(final JsonObject jsonData)
        {
            parse(jsonData);
        }

        private DefaultParams()
        {
            // Not allowed
        }

        private static final String HEAL_INTERVAL = "healthinterval";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            if (jsonElement.has(HEAL_INTERVAL))
            {
                healthPhaseCheck = e -> true;
                final String rawData = jsonElement.get(HEAL_INTERVAL).getAsString();

                final String[] intervals = rawData.split(";");

                for (String interval : intervals)
                {
                    final String[] intervalBoundaries = interval.split("-");
                    if (intervalBoundaries.length != 2)
                    {
                        BrutalBosses.LOGGER.warn("Could not parse AI health requirements, needs exactly two numbers seperated by - for: " + interval);
                        throw new UnsupportedOperationException();
                    }

                    int boundary1 = Integer.parseInt(intervalBoundaries[0]);
                    int boundary2 = Integer.parseInt(intervalBoundaries[1]);

                    if (boundary1 < boundary2)
                    {
                        healthPhaseCheck =
                          healthPhaseCheck.and(e -> (e.getHealth() / e.getMaxHealth()) * 100 > boundary1 && (e.getHealth() / e.getMaxHealth()) * 100 < boundary2);
                    }
                    else
                    {
                        healthPhaseCheck =
                          healthPhaseCheck.and(e -> (e.getHealth() / e.getMaxHealth()) * 100 < boundary1 && (e.getHealth() / e.getMaxHealth()) * 100 > boundary2);
                    }
                }
            }

            return this;
        }
    }
}
