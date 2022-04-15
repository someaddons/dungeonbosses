package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BasePressurePlateBlock.class)
/**
 * Excempts bosses from triggering pressure plates
 */
public class BasePressurePlateBlockMixin
{
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    public void on(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ca)
    {
        if (entity instanceof LivingEntity)
        {
            final BossCapability cap = entity.getCapability(BossCapability.BOSS_CAP).orElse(null);
            if (cap != null)
            {
                if (cap.isBoss())
                {
                    ca.cancel();
                }
            }
        }
    }
}
