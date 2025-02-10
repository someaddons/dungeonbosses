package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Changes gear depending on health
 */
public class ChangeGearGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:changegear");

    private final Mob              mob;
    private       ChangeGearParams params;

    public ChangeGearGoal(Mob mob, final IAIParams params)
    {
        this.params = (ChangeGearParams) params;
        this.mob = mob;
    }

    public boolean canUse()
    {
        return params.healthPhaseCheck.test(mob);
    }

    @Override
    public void start()
    {
        for (final Map.Entry<EquipmentSlot, ItemStack> data : params.gearSet.entrySet())
        {
            mob.setItemSlot(data.getKey(), data.getValue().copy());
        }
    }

    public void tick()
    {

    }

    public static class ChangeGearParams extends IAIParams.DefaultParams
    {
        private Map<EquipmentSlot, ItemStack> gearSet;

        public ChangeGearParams(final JsonObject jsonData)
        {
            super(jsonData);
        }

        public static final String GEAR = "gear";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            gearSet = new HashMap<>();
            if (jsonElement.has(GEAR))
            {
                JsonObject gear = jsonElement.get(GEAR).getAsJsonObject();

                for (Map.Entry<String, JsonElement> data : gear.entrySet())
                {
                    final EquipmentSlot slot = EquipmentSlot.byName(data.getKey());
                    try
                    {
                        final ItemStack stack = ItemStack.of(TagParser.parseTag(data.getValue().getAsString()));
                        gearSet.put(slot, stack);
                    }
                    catch (CommandSyntaxException e)
                    {
                        BrutalBosses.LOGGER.warn("Could not parse item of: " + data.getValue().getAsString(), e);
                        throw new RuntimeException(e);
                    }
                }
            }

            return this;
        }
    }
}