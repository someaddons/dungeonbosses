package com.brutalbosses.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration
{
    public final ForgeConfigSpec                      ForgeConfigSpecBuilder;
    public final ForgeConfigSpec.ConfigValue<Boolean> printChestLoottable;
    public final ForgeConfigSpec.ConfigValue<Double>  globalDifficultyMultiplier;
    public final ForgeConfigSpec.ConfigValue<Integer> globalBossSpawnChance;
    public final ForgeConfigSpec.ConfigValue<Integer> minDistance;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.push("Config category");

        builder.comment(
          "Prints the chest loottable on opening and on spawn and logs the Loottables which do not have a boss assigned of structures to the latest.log. Useful to find the table used by a dungeon chest, only works if the chest is not opened yet: default:false");
        printChestLoottable = builder.define("printChestLoottableOnOpen", false);

        builder.comment("Global difficulty multiplier, affects health and damage of all bosses");
        globalDifficultyMultiplier = builder.defineInRange("globalDifficultyMultiplier", 1, 0.1, 1000);

        builder.comment("Global boss spawn chance, determines the chance per treasure chest spawn at which a boss can appear in a structure. Chance X in 100, default = 30");
        globalBossSpawnChance = builder.defineInRange("globalBossSpawnChance", 30, 1, 100);

        builder.comment("Minimum distance in blocks between spawning, is not 100% guranteed compares last 20 spawns. default = 100");
        minDistance = builder.defineInRange("minDistance", 100, 0, 1000);

        // Escapes the current category level
        builder.pop();
        ForgeConfigSpecBuilder = builder.build();
    }
}
