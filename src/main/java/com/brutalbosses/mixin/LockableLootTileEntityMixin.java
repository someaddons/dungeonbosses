package com.brutalbosses.mixin;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.world.RegionAwareTE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

@Mixin(RandomizableContainerBlockEntity.class)
public class LockableLootTileEntityMixin implements RegionAwareTE
{
    private boolean                            spawnedBoss = false;
    private WeakReference<ServerLevelAccessor> region      = new WeakReference<>(null);

    @Inject(method = "setLootTable(Lnet/minecraft/resources/ResourceLocation;J)V", at = @At("RETURN"))
    private void onSetLoot(final ResourceLocation lootTable, final long seed, final CallbackInfo ci)
    {
        final ServerLevelAccessor world = region.get();
        if (world != null && !spawnedBoss)
        {
            spawnedBoss = true;
            BossSpawnHandler.onChestPlaced(world.getLevel(), (RandomizableContainerBlockEntity) (Object) this);
        }

        region.clear();
    }

    @Inject(method = "tryLoadLootTable", at = @At("RETURN"))
    private void onLoadLoot(final CompoundTag p_184283_1_, final CallbackInfoReturnable<Boolean> cir)
    {
        if (cir.getReturnValue())
        {
            final ServerLevelAccessor world = region.get();
            if (world != null && !spawnedBoss)
            {
                spawnedBoss = true;
                BossSpawnHandler.onChestPlaced(world.getLevel(), (RandomizableContainerBlockEntity) (Object) this);
            }

            region.clear();
        }
    }

    @Override
    public void setRegion(final ServerLevelAccessor region)
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
    @Redirect(method = "setLootTable(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE"
      , target = "Lnet/minecraft/world/level/BlockGetter;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private static BlockEntity setLootTable(
      final BlockGetter reader, final BlockPos pos)
    {
        final BlockEntity te = reader.getBlockEntity(pos);
        if (te instanceof RegionAwareTE && reader instanceof ServerLevelAccessor)
        {
            ((RegionAwareTE) te).setRegion((ServerLevelAccessor) reader);
        }

        return te;
    }
}
