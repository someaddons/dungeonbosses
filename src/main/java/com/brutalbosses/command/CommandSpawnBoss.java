package com.brutalbosses.command;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

/**
 * Print out total network stats
 */
public class CommandSpawnBoss implements Opcommand
{
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final CommandSource source = context.getSource();
        source.sendFailure(new StringTextComponent("Enter a valid boss id(name of the json file)"));
        return 0;
    }

    @Override
    public String getName()
    {
        return "spawnboss";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return
          ICommand.newLiteral(getName())
            .then(ICommand.newArgument("bossID", StringArgumentType.word()).executes(this::executeSpawnBoss)).executes(this::checkPreConditionAndExecute);
    }

    private int executeSpawnBoss(final CommandContext<CommandSource> context)
    {
        final CommandSource source = context.getSource();
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
            source.sendFailure(new StringTextComponent("Enter a valid boss id(name of the json file), no boss found for:" + bossID));
            return 0;
        }

        BossSpawnHandler.spawnBoss(source.getLevel(), new BlockPos(source.getPosition()), bossType, null);

        return 0;
    }
}
