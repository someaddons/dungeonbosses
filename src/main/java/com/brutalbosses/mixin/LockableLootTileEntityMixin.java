package com.brutalbosses.mixin;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.world.RegionAwareTE;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

@Mixin(LockableLootTileEntity.class)
public class LockableLootTileEntityMixin implements RegionAwareTE
{
    private boolean                     spawnedBoss = false;
    private WeakReference<IServerWorld> region      = new WeakReference<>(null);

    @Inject(method = "setLootTable(Lnet/minecraft/util/ResourceLocation;J)V", at = @At("RETURN"))
    private void onSetLoot(final ResourceLocation lootTable, final long seed, final CallbackInfo ci)
    {
        final IServerWorld world = region.get();
        if (world != null && !spawnedBoss)
        {
            spawnedBoss = true;
            BossSpawnHandler.onChestPlaced(world, (LockableLootTileEntity) (Object) this);
        }

        region.clear();
    }

    @Inject(method = "tryLoadLootTable", at = @At("RETURN"))
    private void onLoadLoot(final CompoundNBT p_184283_1_, final CallbackInfoReturnable<Boolean> cir)
    {
        if (cir.getReturnValue())
        {
            final IServerWorld world = region.get();
            if (world != null && !spawnedBoss)
            {
                spawnedBoss = true;
                BossSpawnHandler.onChestPlaced(world, (LockableLootTileEntity) (Object) this);
            }

            region.clear();
        }
    }

    @Override
    public void setRegion(final IServerWorld region)
    {
        this.region = new WeakReference<>(region);
    }

    /**
     * For mods spawning on the mainthread like castle dungeons
     *
     * @param reader
     * @param pos
     * @return
     */
    @Redirect(method = "setLootTable(Lnet/minecraft/world/IBlockReader;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/ResourceLocation;)V", at = @At(value = "INVOKE"
      , target = "Lnet/minecraft/world/IBlockReader;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
    private static TileEntity setLootTable(
      final IBlockReader reader, final BlockPos pos)
    {
        final TileEntity te = reader.getBlockEntity(pos);
        if (te instanceof RegionAwareTE && reader instanceof IServerWorld)
        {
            ((RegionAwareTE) te).setRegion((IServerWorld) reader);
        }

        return te;
    }
}
