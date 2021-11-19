package com.brutalbosses.mixin;

import com.brutalbosses.entity.BossSpawnHandler;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILHARD;

/**
 * Hook to spawn a random boss for a structure containing dungeon loot
 */
@Mixin(Template.class)
public class TemplateLootSpawnMixin
{
    @Inject(method = "placeInWorld(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;Ljava/util/Random;I)Z",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;load(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V", shift = At.Shift.AFTER),
      locals = CAPTURE_FAILHARD)
    public void on(
      final IServerWorld world,
      final BlockPos start,
      final BlockPos end,
      final PlacementSettings placementSettings,
      final Random random,
      final int setBlockFlag,
      final CallbackInfoReturnable<Boolean> cir,
      List list,
      MutableBoundingBox mutableboundingbox,
      List list1,
      List list2,
      int i,
      int j,
      int k,
      int l,
      int i1,
      int j1,
      Iterator var17,
      Template.BlockInfo template$blockinfo,
      BlockPos blockpos,
      FluidState fluidstate,
      BlockState blockstate,
      TileEntity tileentity1)
    {
        if (tileentity1 instanceof LockableLootTileEntity)
        {
            BossSpawnHandler.onChestPlaced(world, (LockableLootTileEntity) tileentity1);
        }
    }
}
