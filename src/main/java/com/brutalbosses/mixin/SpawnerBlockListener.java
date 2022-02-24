package com.brutalbosses.mixin;

import com.brutalbosses.world.PostStructureInfoGetter;
import com.brutalbosses.world.RegionAwareTE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class SpawnerBlockListener implements PostStructureInfoGetter
{
    private StructureFeature<?> current = null;

    @Override
    public StructureFeature<?> getStructure()
    {
        return current;
    }

    @Override
    public void setCurrent(final StructureFeature<?> current)
    {
        this.current = current;
    }

    @Inject(method = "getBlockEntity", at = @At("RETURN"))
    private void onGetTE(final BlockPos pos, final CallbackInfoReturnable<BlockEntity> cir)
    {
        final BlockEntity te = cir.getReturnValue();
        if (te instanceof RegionAwareTE)
        {
            ((RegionAwareTE) te).setRegion((ServerLevelAccessor) this);
        }
    }
}
