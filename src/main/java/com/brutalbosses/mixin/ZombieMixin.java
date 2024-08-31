package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster
{
    protected ZombieMixin(final EntityType<? extends Monster> p_33002_, final Level p_33003_)
    {
        super(p_33002_, p_33003_);
    }

    @Inject(method = "convertsInWater", at = @At("HEAD"), cancellable = true)
    private void onBossConvert(final CallbackInfoReturnable<Boolean> cir)
    {
        final BossCapability cap = this.getCapability(BossCapability.BOSS_CAP).orElse(null);
        if (cap != null && cap.isBoss())
        {
            cir.setReturnValue(false);
        }
    }
}
