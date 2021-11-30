package com.brutalbosses.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;

public interface ICommand
{
    static final int OP_PERM_LEVEL = 4;

    default LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral(getName()).executes(this::checkPreConditionAndExecute);
    }

    static LiteralArgumentBuilder<CommandSource> newLiteral(final String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }

    static <T> RequiredArgumentBuilder<CommandSource, T> newArgument(final String name, final ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    default int checkPreConditionAndExecute(final CommandContext<CommandSource> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        return onExecute(context);
    }

    default ICommandCallbackBuilder<CommandSource> executePreConditionCheck()
    {
        return executeCallback -> context -> {
            if (!checkPreCondition(context))
            {
                return 0;
            }

            return executeCallback.run(context);
        };
    }

    default boolean checkPreCondition(final CommandContext<CommandSource> context)
    {
        return context.getSource().getEntity() instanceof PlayerEntity || context.getSource().hasPermission(OP_PERM_LEVEL);
    }

    int onExecute(final CommandContext<CommandSource> context);

    String getName();

    interface ICommandCallbackBuilder<S>
    {

        Command<S> then(final Command<CommandSource> executeCallback);
    }
}
