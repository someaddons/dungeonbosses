package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapEntity;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieMixin
{
    @Inject(method = "convertsInWater", at = @At("HEAD"), cancellable = true)
    private void onBossConvert(final CallbackInfoReturnable<Boolean> cir)
    {
        if (this instanceof BossCapEntity bossCapEntity && bossCapEntity.getBossCap() != null)
        {
            cir.setReturnValue(false);
        }
    }
}
