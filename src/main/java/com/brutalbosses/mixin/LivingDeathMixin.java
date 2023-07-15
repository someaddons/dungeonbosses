package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.event.EventHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingDeathMixin {

    @Inject(method = "die", at = @At("RETURN"))
    private void onDeath(final DamageSource damageSource, final CallbackInfo ci)
    {
        final BossCapability cap = ((BossCapEntity)(Object)this).getBossCap();
        if (cap != null && cap.isBoss()) {
            EventHandler.onBossDeath((LivingEntity)(Object)this, cap, damageSource);
        }
    }

}
