package com.brutalbosses.config;

import com.brutalbosses.BrutalBosses;
import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;

public class CommonConfiguration implements ICommonConfig
{
    public boolean printChestLoottable        = false;
    public double  globalDifficultyMultiplier = 1.0;
    public int     globalBossSpawnChance      = 30;
    public int     minDistance                = 100;

    public CommonConfiguration()
    {
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:",
          "Prints the chest loottable on opening and on spawn and logs the Loottables which do not have a boss assigned of structures to the latest.log. Useful to find the table used by a dungeon chest, only works if the chest is not opened yet: default:false");
        entry.addProperty("printChestLoottable", printChestLoottable);
        root.add("printChestLoottable", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Global difficulty multiplier, affects health and damage of all bosses, default = 1.0, max = 1000");
        entry2.addProperty("globalDifficultyMultiplier", globalDifficultyMultiplier);
        root.add("globalDifficultyMultiplier", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:",
          "Global boss spawn chance, determines the chance per treasure chest spawn at which a boss can appear in a structure. Chance X in 100, default = 30");
        entry3.addProperty("globalBossSpawnChance", globalBossSpawnChance);
        root.add("globalBossSpawnChance", entry3);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:", "Minimum distance in blocks between spawning, is not 100% guranteed compares last 20 spawns. default = 100");
        entry4.addProperty("minDistance", minDistance);
        root.add("minDistance", entry4);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        if (data == null)
        {
            BrutalBosses.LOGGER.error("Config file was empty!");
            return;
        }

        printChestLoottable = data.get("printChestLoottable").getAsJsonObject().get("printChestLoottable").getAsBoolean();
        globalDifficultyMultiplier = data.get("globalDifficultyMultiplier").getAsJsonObject().get("globalDifficultyMultiplier").getAsDouble();
        globalBossSpawnChance = data.get("globalBossSpawnChance").getAsJsonObject().get("globalBossSpawnChance").getAsInt();
        minDistance = data.get("minDistance").getAsJsonObject().get("minDistance").getAsInt();
    }
}
