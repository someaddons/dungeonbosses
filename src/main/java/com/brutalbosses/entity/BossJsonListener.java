package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ai.IAIParams;
import com.brutalbosses.event.EventHandler;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Loads and listens to custom visitor data added
 */
public class BossJsonListener extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener
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
    public static final String ENTITY_NBT       = "entitynbt";
    public static final String MobEffectS       = "effects";
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
    public static final String NAME_VISIBLE     = "namevisible";
    public static final String SCALE            = "visualscale";
    public static final String EXP              = "experience";
    public static final String ITEM_LOOT_COUNT  = "droppeditemamount";
    public static final String SHOW_BAR         = "showbossbar";
    private static final String PROTECT_TREASURE = "protect_treasure";
    private static final String RANDOM_SPAWN = "random_spawn";

    /**
     * Random
     */
    private static final Random rand = new Random();

    public static final    BossJsonListener      instance              = new BossJsonListener();
    public static volatile LayeredRegistryAccess layeredRegistryAccess = null;

    public BossJsonListener()
    {
        super(GSON, "bosses");
    }

    @Override
    protected void apply(
        final Map<ResourceLocation, JsonElement> jsonElementMap, final ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        final ImmutableMap.Builder<ResourceLocation, BossType> bossTypes = ImmutableMap.<ResourceLocation, BossType>builder();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getKey().getNamespace().equals(BrutalBosses.MOD_ID))
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

            final RegistryOps<Tag> nbtOpsWithContext = layeredRegistryAccess.compositeAccess().createSerializationContext(NbtOps.INSTANCE);

            EntityType entityTypeEntry = null;
            if (data.has(ENTITY))
            {
                ResourceLocation entityType = ResourceLocation.tryParse(data.get(ENTITY).getAsString());
                if (entityType == null)
                {
                    BrutalBosses.LOGGER.error("Missing or malformed field  entity in bossfile:" + entry.getKey());
                    return null;
                }

                entityTypeEntry = BuiltInRegistries.ENTITY_TYPE.get(entityType);
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

            if (data.has(ENTITY_NBT))
            {
                try
                {
                    bossType.setEntityNBT(TagParser.parseTag(data.get(ENTITY_NBT).getAsString()));
                }
                catch (CommandSyntaxException e)
                {
                    BrutalBosses.LOGGER.error("Malformed field entitynbt in bossfile:" + entry.getKey(), e);
                    return null;
                }
            }

            if (data.has(Name))
            {
                bossType.setDesc(data.get(Name).getAsString());
            }

            if (data.has(NAME_VISIBLE))
            {
                bossType.setNameVisible(data.get(Name).getAsBoolean());
            }

            if (data.has(PROTECT_TREASURE))
            {
                bossType.setProtectsTreasure(data.get(PROTECT_TREASURE).getAsBoolean());
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

            if (data.has(MobEffectS))
            {
                final ImmutableMap.Builder<Holder<MobEffect>, Integer> MobEffects = ImmutableMap.builder();
                final JsonElement MobEffectData = data.get(MobEffectS);

                for (final Map.Entry<String, JsonElement> effectEntry : MobEffectData.getAsJsonObject().entrySet())
                {
                    final ResourceLocation effectId = ResourceLocation.tryParse(effectEntry.getKey());
                    final Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectId).get();
                    if (effect == null)
                    {
                        BrutalBosses.LOGGER.error("Bad MobEffect id:" + effectId + " in:" + entry.getKey());
                        return null;
                    }
                    MobEffects.put(effect, effectEntry.getValue().getAsInt());
                }

                bossType.setMobEffects(MobEffects.build());
            }

            if (data.has(STATS))
            {
                final ImmutableMap.Builder<Holder<Attribute>, Float> attributeModifiers = ImmutableMap.builder();
                final JsonElement MobEffectData = data.get(STATS);

                for (final Map.Entry<String, JsonElement> statsEntry : MobEffectData.getAsJsonObject().entrySet())
                {
                    final Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.tryParse(statsEntry.getKey())).get();
                    if (attribute == null)
                    {
                        BrutalBosses.LOGGER.error("Bad attribute id:" + ResourceLocation.tryParse(statsEntry.getKey()) + " in:" + entry.getKey());
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
                final JsonElement MobEffectData = data.get(CUSTOMSTATS);

                for (final Map.Entry<String, JsonElement> statsEntry : MobEffectData.getAsJsonObject().entrySet())
                {
                    final float modifier = statsEntry.getValue().getAsFloat();
                    customAttributeBuilder.put(statsEntry.getKey(), modifier);
                }

                bossType.setCustomAttributes(customAttributeBuilder.build());
            }

            if (data.has(GEAR))
            {
                final ImmutableMap.Builder<EquipmentSlot, ItemStack> gearList = ImmutableMap.<EquipmentSlot, ItemStack>builder();
                final JsonElement jsonEntry = data.get(GEAR);

                if (jsonEntry.isJsonObject())
                {
                    final JsonObject gearData = jsonEntry.getAsJsonObject();
                    if (gearData.has(MAINHAND))
                    {
                        gearList.put(EquipmentSlot.MAINHAND, ItemStack.CODEC.parse(nbtOpsWithContext, (TagParser.parseTag(gearData.get(MAINHAND).getAsString()))).getOrThrow());
                    }

                    if (gearData.has(OFFHAND))
                    {
                        gearList.put(EquipmentSlot.OFFHAND, ItemStack.CODEC.parse(nbtOpsWithContext, (TagParser.parseTag(gearData.get(OFFHAND).getAsString()))).getOrThrow());
                    }

                    if (gearData.has(HELMET))
                    {
                        gearList.put(EquipmentSlot.HEAD, ItemStack.CODEC.parse(nbtOpsWithContext, (TagParser.parseTag(gearData.get(HELMET).getAsString()))).getOrThrow());
                    }

                    if (gearData.has(CHESTPLATE))
                    {
                        gearList.put(EquipmentSlot.CHEST, ItemStack.CODEC.parse(nbtOpsWithContext, TagParser.parseTag(gearData.get(CHESTPLATE).getAsString())).getOrThrow());
                    }

                    if (gearData.has(LEGGINGS))
                    {
                        gearList.put(EquipmentSlot.LEGS, ItemStack.CODEC.parse(nbtOpsWithContext, (TagParser.parseTag(gearData.get(LEGGINGS).getAsString()))).getOrThrow());
                    }

                    if (gearData.has(FEET))
                    {
                        gearList.put(EquipmentSlot.FEET, ItemStack.CODEC.parse(nbtOpsWithContext, (TagParser.parseTag(gearData.get(FEET).getAsString()))).getOrThrow());
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
                    final ResourceLocation aiID = ResourceLocation.tryParse(aiEntry.getKey());
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
                    final ResourceLocation lootTableID = ResourceLocation.tryParse(spawnLootTableEntry.getKey());
                    spawnMap.put(lootTableID, spawnLootTableEntry.getValue().getAsInt());
                }

                bossType.setSpawnTable(spawnMap.build());
            }

            if (data.has(SHOW_BAR))
            {
                bossType.setBossBar(data.get(SHOW_BAR).getAsBoolean());
            }

            if (data.has(RANDOM_SPAWN))
            {
                int chance = data.get(RANDOM_SPAWN).getAsInt();

                if (chance < 0 || chance > 100)
                {
                    BrutalBosses.LOGGER.error("Bad random spawn chance in bossfile:" + entry.getKey() + " chance:" + chance + " has to be within 0-100");
                }

                chance = Math.min(100, Math.max(0, chance));

                Set<BossType> possibleBosses = EventHandler.randomSpawns.get(bossType.getEntityType());

                if (possibleBosses == null)
                {
                    possibleBosses = new HashSet<>();
                }

                possibleBosses.add(bossType);
                EventHandler.randomSpawns.put(entityTypeEntry, possibleBosses);
                bossType.setRandomSpawnChance(chance);
            }

            return bossType;
        }
        catch (Exception e)
        {
            BrutalBosses.LOGGER.warn("Could not parse boss for:" + entry.getKey(), e);
        }

        return null;
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return BrutalBosses.id("jsonlistener");
    }
}
