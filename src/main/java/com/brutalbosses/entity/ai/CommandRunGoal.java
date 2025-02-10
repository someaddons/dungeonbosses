package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Changes gear depending on health
 */
public class CommandRunGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:runcommand");

    private final Mob           mob;
    private       CommandParams params;
    private       LivingEntity  target      = null;
    private       int           cooldown    = 0;
    private       int           combatTimer = 0;

    public CommandRunGoal(Mob mob, final IAIParams params)
    {
        this.params = (CommandParams) params;
        this.mob = mob;
    }

    @Override
    public boolean canUse()
    {
        if (cooldown > 0)
        {
            cooldown--;
        }

        if (combatTimer > 0)
        {
            combatTimer--;
        }

        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            combatTimer = 20 * 30;

            if (cooldown > 0)
            {
                return false;
            }

            this.target = target;
            return params.healthPhaseCheck.test(mob);
        }
        else
        {
            if (combatTimer == 0)
            {
                // Reset command cooldown out of combat
                cooldown = 0;
            }

            return false;
        }
    }

    @Override
    public void start()
    {
        cooldown = params.cooldown;
        try
        {
            mob.level().getServer().getCommands().getDispatcher().execute(params.command, new CommandSourceStack(mob, mob.position(), mob.getRotationVector(),
                (ServerLevel) mob.level(), 4, "Server", Component.literal("brutalbosses:aicommand"), mob.level.getServer(), null).withEntity(mob));
        }
        catch (CommandSyntaxException e)
        {
            BrutalBosses.LOGGER.warn("Failed to run ai command: " + params.command, e);
        }
    }

    public void tick()
    {

    }

    public static class CommandParams extends IAIParams.DefaultParams
    {
        private String command;
        private int    cooldown;

        public CommandParams(final JsonObject jsonData)
        {
            super(jsonData);
        }

        private static final String COOLDOWN = "cooldown";
        public static final  String COMMAND  = "command";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            command = "";
            if (jsonElement.has(COMMAND))
            {
                command = jsonElement.get(COMMAND).getAsString();
            }

            if (jsonElement.has(COOLDOWN))
            {
                cooldown = jsonElement.get(COOLDOWN).getAsInt();
            }

            return this;
        }
    }
}