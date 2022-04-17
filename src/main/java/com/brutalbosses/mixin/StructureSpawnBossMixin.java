package com.brutalbosses.mixin;

import com.brutalbosses.world.PostStructureInfoGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(StructureStart.class)
/**
 * Sets the currently generating structure
 */
public class StructureSpawnBossMixin
{
    @Shadow
    @Final
    private ConfiguredStructureFeature<?, ?> feature;

    @Inject(method = "placeInChunk", at = @At("HEAD"))
    public void beforeStructure(
      WorldGenLevel worldGenRegion,
      StructureFeatureManager manager,
      ChunkGenerator p_73586_,
      Random p_73587_,
      BoundingBox p_73588_,
      ChunkPos p_73589_,
      CallbackInfo info)
    {
        final PostStructureInfoGetter infoGetter = (PostStructureInfoGetter) worldGenRegion;
        infoGetter.setCurrent(feature);
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    public void afterStructure(
      WorldGenLevel worldGenRegion,
      StructureFeatureManager manager,
      ChunkGenerator p_73586_,
      Random p_73587_,
      BoundingBox p_73588_,
      ChunkPos p_73589_,
      CallbackInfo info)
    {
        final PostStructureInfoGetter infoGetter = (PostStructureInfoGetter) worldGenRegion;
        infoGetter.setCurrent(null);
    }
}
