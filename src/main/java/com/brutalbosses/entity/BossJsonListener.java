package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ai.IAIParams;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.potion.Effect;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Loads and listens to custom visitor data added
 */
public class BossJsonListener extends JsonReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    public static final String ID               = "id";
    public static final String ENTITY           = "entity";
    public static final String EFFECTS          = "effects";
    public static final String STATS            = "attributes";
    public static final String CUSTOMSTATS      = "customattributes";
    public static final String GEAR             = "gear";
    public static final String MAINHAND         = "mainhand";
    public static final String OFFHAND          = "offhand";
    public static final String HELMET           = "helmet";
    public static final String CHESTPLATE       = "chestplate";
    public static final String LEGGINGS         = "leggings";
    public static final String FEET             = "feet";
    public static final String AI               = "ai-goals";
    public static final String SPAWNCHESTTABLES = "spawnatchest";
    public static final String Name             = "name";
    public static final String SCALE            = "visualscale";
    public static final String EXP              = "experience";
    public static final String ITEM_LOOT_COUNT  = "droppeditemamount";

    /**
     * Random
     */
    private static final Random rand = new Random();

    public BossJsonListener()
    {
        super(GSON, "bosses");
    }

    @Override
    protected void apply(
      final Map<ResourceLocation, JsonElement> jsonElementMap, final IResourceManager resourceManager, final IProfiler profiler)
    {
        final ImmutableMap.Builder<ResourceLocation, BossType> bossTypes = ImmutableMap.<ResourceLocation, BossType>builder();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getKey().getNamespace().equals(BrutalBosses.MODID))
            {
                continue;
            }

            final BossType bossType = tryParse(entry);
            if (bossType != null)
            {
                bossTypes.put(bossType.getID(), bossType);
            }
        }

        BossTypeManager.instance.setBossTypes(bossTypes.build());
        BossTypeManager.instance.afterLoad();
    }

    /**
     * Tries to parse the entry
     *
     * @param entry
     */
    private BossType tryParse(final Map.Entry<ResourceLocation, JsonElement> entry)
    {
        try
        {
            final JsonObject data = (JsonObject) entry.getValue();

            final ResourceLocation bossID = entry.getKey();

            EntityType entityTypeEntry = null;
            if (data.has(ENTITY))
            {
                ResourceLocation entityType = ResourceLocation.tryParse(data.get(ENTITY).getAsString());
                if (entityType == null)
                {
                    BrutalBosses.LOGGER.error("Missing or malformed field  entity in bossfile:" + entry.getKey());
                    return null;
                }

                entityTypeEntry = ForgeRegistries.ENTITIES.getValue(entityType);
                if (entityTypeEntry == EntityType.PIG)
                {
                    BrutalBosses.LOGGER.error("Cannot find entity type for:" + entityType + " id in bossfile:" + entry.getKey());
                    return null;
                }
            }
            else
            {
                BrutalBosses.LOGGER.error("Missing or malformed field  entity in bossfile:" + entry.getKey());
                return null;
            }

            final BossType bossType = BossTypeManager.instance.bosses.getOrDefault(bossID, new BossType(entityTypeEntry, bossID));

            if (data.has(Name))
            {
                bossType.setDesc(data.get(Name).getAsString());
            }

            if (data.has(SCALE))
            {
                bossType.setVisualScale(data.get(SCALE).getAsFloat());
            }

            if (data.has(EXP))
            {
                bossType.setExperienceDrop(data.get(EXP).getAsInt());
            }

            if (data.has(ITEM_LOOT_COUNT))
            {
                bossType.setItemLootCount(data.get(ITEM_LOOT_COUNT).getAsInt());
            }

            if (data.has(EFFECTS))
            {
                final ImmutableMap.Builder<Effect, Integer> effects = ImmutableMap.<Effect, Integer>builder();
                final JsonElement effectData = data.get(EFFECTS);

                for (final Map.Entry<String, JsonElement> effectEntry : effectData.getAsJsonObject().entrySet())
                {
                    final ResourceLocation effectID = new ResourceLocation(effectEntry.getKey());
                    final Effect effect = ForgeRegistries.POTIONS.getValue(effectID);
                    if (effect == null)
                    {
                        BrutalBosses.LOGGER.error("Bad effect id:" + effectID + " in:" + entry.getKey());
                        return null;
                    }
                    effects.put(effect, effectEntry.getValue().getAsInt());
                }

                bossType.setEffects(effects.build());
            }

            if (data.has(STATS))
            {
                final ImmutableMap.Builder<Attribute, Float> attributeModifiers = ImmutableMap.<Attribute, Float>builder();
                final JsonElement effectData = data.get(STATS);

                for (final Map.Entry<String, JsonElement> statsEntry : effectData.getAsJsonObject().entrySet())
                {
                    final Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(statsEntry.getKey()));
                    if (attribute == null)
                    {
                        BrutalBosses.LOGGER.error("Bad attribute id:" + new ResourceLocation(statsEntry.getKey()) + " in:" + entry.getKey());
                        continue;
                    }

                    final float modifier = statsEntry.getValue().getAsFloat();
                    attributeModifiers.put(attribute, modifier);
                }

                bossType.setAttributes(attributeModifiers.build());
            }

            if (data.has(CUSTOMSTATS))
            {
                final ImmutableMap.Builder<String, Float> customAttributeBuilder = ImmutableMap.<String, Float>builder();
                final JsonElement effectData = data.get(CUSTOMSTATS);

                for (final Map.Entry<String, JsonElement> statsEntry : effectData.getAsJsonObject().entrySet())
                {
                    final float modifier = statsEntry.getValue().getAsFloat();
                    customAttributeBuilder.put(statsEntry.getKey(), modifier);
                }

                bossType.setCustomAttributes(customAttributeBuilder.build());
            }

            if (data.has(GEAR))
            {
                final ImmutableMap.Builder<EquipmentSlotType, ItemStack> gearList = ImmutableMap.<EquipmentSlotType, ItemStack>builder();
                final JsonElement jsonEntry = data.get(GEAR);

                if (jsonEntry.isJsonObject())
                {
                    final JsonObject gearData = jsonEntry.getAsJsonObject();
                    if (gearData.has(MAINHAND))
                    {
                        gearList.put(EquipmentSlotType.MAINHAND, ItemStack.of(JsonToNBT.parseTag(gearData.get(MAINHAND).getAsString())));
                    }

                    if (gearData.has(OFFHAND))
                    {
                        gearList.put(EquipmentSlotType.OFFHAND, ItemStack.of(JsonToNBT.parseTag(gearData.get(OFFHAND).getAsString())));
                    }

                    if (gearData.has(HELMET))
                    {
                        gearList.put(EquipmentSlotType.HEAD, ItemStack.of(JsonToNBT.parseTag(gearData.get(HELMET).getAsString())));
                    }

                    if (gearData.has(CHESTPLATE))
                    {
                        gearList.put(EquipmentSlotType.CHEST, ItemStack.of(JsonToNBT.parseTag(gearData.get(CHESTPLATE).getAsString())));
                    }

                    if (gearData.has(LEGGINGS))
                    {
                        gearList.put(EquipmentSlotType.LEGS, ItemStack.of(JsonToNBT.parseTag(gearData.get(LEGGINGS).getAsString())));
                    }

                    if (gearData.has(FEET))
                    {
                        gearList.put(EquipmentSlotType.FEET, ItemStack.of(JsonToNBT.parseTag(gearData.get(FEET).getAsString())));
                    }
                }

                bossType.setGear(gearList.build());
            }

            if (data.has(AI))
            {
                final ImmutableMap.Builder<ResourceLocation, IAIParams> aiParamMap = ImmutableMap.<ResourceLocation, IAIParams>builder();
                final JsonElement aiData = data.get(AI);

                for (final Map.Entry<String, JsonElement> aiEntry : aiData.getAsJsonObject().entrySet())
                {
                    final ResourceLocation aiID = new ResourceLocation(aiEntry.getKey());
                    if (!BossTypeManager.instance.aiCreatorRegistry.containsKey(aiID))
                    {
                        BrutalBosses.LOGGER.error("Unkown AI id:" + aiID + " in:" + entry.getKey());
                        continue;
                    }

                    final Function<JsonObject, IAIParams> paramReader = BossTypeManager.instance.aiParamParsers.get(aiID);

                    IAIParams params = IAIParams.EMPTY;
                    if (paramReader != null && aiEntry.getValue().isJsonObject())
                    {
                        params = paramReader.apply(aiEntry.getValue().getAsJsonObject());
                    }

                    aiParamMap.put(aiID, params);
                }

                bossType.setAIData(aiParamMap.build());
            }

            if (data.has(SPAWNCHESTTABLES))
            {
                final ImmutableMap.Builder<ResourceLocation, Integer> spawnMap = ImmutableMap.<ResourceLocation, Integer>builder();
                final JsonElement spawnLootableData = data.get(SPAWNCHESTTABLES);

                for (final Map.Entry<String, JsonElement> spawnLootTableEntry : spawnLootableData.getAsJsonObject().entrySet())
                {
                    final ResourceLocation lootTableID = new ResourceLocation(spawnLootTableEntry.getKey());
                    spawnMap.put(lootTableID, spawnLootTableEntry.getValue().getAsInt());
                }

                bossType.setSpawnTable(spawnMap.build());
            }

            return bossType;
        }
        catch (Exception e)
        {
            BrutalBosses.LOGGER.warn("Could not parse boss for:" + entry.getKey(), e);
        }

        return null;
    }
}
