package com.brutalbosses.mixin;

import com.brutalbosses.world.PostStructureInfoGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(ChunkGenerator.class)
/**
 * Sets the currently generating structure
 */
public class BiomeSpawnBossMixin
{
    @Inject(method = "applyBiomeDecoration", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void beforeStructure(
      final WorldGenLevel worldGenRegion,
      final ChunkAccess supplier,
      final StructureFeatureManager structurefeature,
      final CallbackInfo ci,
      final ChunkPos chunkpos,
      final SectionPos sectionpos,
      final BlockPos blockpos,
      final Map map,
      final List list,
      final WorldgenRandom worldgenrandom,
      final long i,
      final Set set,
      final int j,
      final Registry registry,
      final Registry registry1,
      final int k,
      final int l,
      final int i1,
      final Iterator var19,
      final StructureFeature structurefeature1, final Supplier supplier2)
    {
        final PostStructureInfoGetter infoGetter = (PostStructureInfoGetter) worldGenRegion;
        infoGetter.setCurrent(structurefeature1);
    }

    @Inject(method = "applyBiomeDecoration", at = @At(value = "RETURN"))
    public void afterStructures(final WorldGenLevel worldGenRegion, final ChunkAccess p_187713_, final StructureFeatureManager p_187714_, final CallbackInfo ci)
    {
        final PostStructureInfoGetter infoGetter = (PostStructureInfoGetter) worldGenRegion;
        infoGetter.setCurrent(null);
    }
}
