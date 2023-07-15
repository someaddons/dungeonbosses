package com.brutalbosses.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface Opcommand extends ICommand
{
    @Override
    default boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        if (context.getSource().hasPermission(2))
        {
            return true;
        }

        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return false;
        }

        if (!isPlayerOped((Player) sender))
        {
            return false;
        }
        return true;
    }

    static boolean isPlayerOped(final Player player)
    {
        if (player.getServer() == null)
        {
            return false;
        }

        return player.getServer().getPlayerList().isOp(player.getGameProfile());
    }
}
