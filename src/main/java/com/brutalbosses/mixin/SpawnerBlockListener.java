package com.brutalbosses.mixin;

import com.brutalbosses.world.PostStructureInfoGetter;
import com.brutalbosses.world.RegionAwareTE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class SpawnerBlockListener implements PostStructureInfoGetter
{
    private Structure<?> current = null;

    @Override
    public Structure<?> getStructure()
    {
        return current;
    }

    @Override
    public void setCurrent(final Structure<?> current)
    {
        this.current = current;
    }

    @Inject(method = "getBlockEntity", at = @At("RETURN"))
    private void onGetTE(final BlockPos pos, final CallbackInfoReturnable<TileEntity> cir)
    {
        final TileEntity te = cir.getReturnValue();
        if (te instanceof RegionAwareTE)
        {
            ((RegionAwareTE) te).setRegion((IServerWorld) this);
        }
    }
}
