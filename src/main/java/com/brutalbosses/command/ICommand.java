package com.brutalbosses.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public interface ICommand
{
    static final int OP_PERM_LEVEL = 4;

    default LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(getName()).executes(this::checkPreConditionAndExecute);
    }

    static LiteralArgumentBuilder<CommandSourceStack> newLiteral(final String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }

    static <T> RequiredArgumentBuilder<CommandSourceStack, T> newArgument(final String name, final ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    default int checkPreConditionAndExecute(final CommandContext<CommandSourceStack> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        return onExecute(context);
    }

    default ICommandCallbackBuilder<CommandSourceStack> executePreConditionCheck()
    {
        return executeCallback -> context -> {
            if (!checkPreCondition(context))
            {
                return 0;
            }

            return executeCallback.run(context);
        };
    }

    default boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        return context.getSource().getEntity() instanceof Player || context.getSource().hasPermission(OP_PERM_LEVEL);
    }

    int onExecute(final CommandContext<CommandSourceStack> context);

    String getName();

    interface ICommandCallbackBuilder<S>
    {

        Command<S> then(final Command<CommandSourceStack> executeCallback);
    }
}
