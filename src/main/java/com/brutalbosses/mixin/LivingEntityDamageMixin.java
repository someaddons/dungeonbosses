package com.brutalbosses.mixin;

import com.brutalbosses.event.EventHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin {

    @ModifyVariable(method = "hurt", argsOnly = true, at = @At("HEAD"), ordinal = 0)
    private float brutalbosses$onhurt(float damageOrg, final DamageSource source, final float damage) {
        return EventHandler.applyProjectileDamageBoost(source, (LivingEntity) (Object) this, damageOrg);
    }

}
