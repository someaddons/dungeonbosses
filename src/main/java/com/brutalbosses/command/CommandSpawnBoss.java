package com.brutalbosses.command;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Print out total network stats
 */
public class CommandSpawnBoss implements Opcommand
{
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        source.sendFailure(Component.literal("Enter a valid boss id(name of the json file)"));
        return 0;
    }

    @Override
    public String getName()
    {
        return "spawnboss";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return
          ICommand.newLiteral(getName())
            .then(ICommand.newArgument("bossID", StringArgumentType.word()).executes(this::executeSpawnBoss)).executes(this::checkPreConditionAndExecute);
    }

    private int executeSpawnBoss(final CommandContext<CommandSourceStack> context)
    {
        try
        {
            final CommandSourceStack source = context.getSource();
            if (!checkPreCondition(context))
            {
                return 0;
            }

            final String bossName = StringArgumentType.getString(context, "bossID");

            if (bossName.equals("random"))
            {
                BossSpawnHandler.spawnRandomBoss(source.getLevel(), new BlockPos(source.getPosition()));
                return 0;
            }

            if (bossName.equals("all"))
            {
                for (final BossType type : BossTypeManager.instance.bosses.values())
                {
                    BossSpawnHandler.spawnBoss(source.getLevel(), new BlockPos(source.getPosition()), type, null);
                }
                return 0;
            }

            final ResourceLocation bossID = new ResourceLocation("brutalbosses", bossName);
            final BossType bossType = BossTypeManager.instance.bosses.get(bossID);
            if (bossType == null)
            {
                source.sendFailure(Component.literal("Enter a valid boss id(name of the json file), no boss found for:" + bossID));
                return 0;
            }

            BossSpawnHandler.spawnBoss(source.getLevel(), new BlockPos(source.getPosition()), bossType, null);
        }
        catch (Error e)
        {
            BrutalBosses.LOGGER.error(e);
        }
        return 0;
    }
}
