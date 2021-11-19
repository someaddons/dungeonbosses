package com.brutalbosses.mixin;

import com.brutalbosses.world.PostStructureInfoGetter;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(Biome.class)
/**
 * Sets the currently generating structure
 */
public class BiomeSpawnBossMixin
{
    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void beforeStructure(
      final StructureManager structureManager,
      final ChunkGenerator chunkGenerator,
      final WorldGenRegion worldGenRegion,
      final long p_242427_4_,
      final SharedSeedRandom p_242427_6_, final BlockPos pos, final CallbackInfo ci, List list, int i, int j, int k, Iterator var12, Structure structure)
    {
        final PostStructureInfoGetter infoGetter = (PostStructureInfoGetter) worldGenRegion;
        infoGetter.setCurrent(structure);
    }

    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/structure/StructureManager;startsForFeature(Lnet/minecraft/util/math/SectionPos;Lnet/minecraft/world/gen/feature/structure/Structure;)Ljava/util/stream/Stream;", shift = At.Shift.AFTER))
    public void afterStructures(
      final StructureManager structureManager,
      final ChunkGenerator chunkGenerator,
      final WorldGenRegion worldGenRegion,
      final long p_242427_4_,
      final SharedSeedRandom p_242427_6_, final BlockPos pos, final CallbackInfo ci)
    {
        final PostStructureInfoGetter infoGetter = (PostStructureInfoGetter) worldGenRegion;
        infoGetter.setCurrent(null);
    }
}
