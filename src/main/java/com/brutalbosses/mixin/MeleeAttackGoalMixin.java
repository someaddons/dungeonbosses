package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomAttributes;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
/**
 * Attack reach and cooldown changes
 */
public class MeleeAttackGoalMixin {

    @Shadow
    private int ticksUntilNextAttack;
    @Shadow
    @Final
    protected PathfinderMob mob;
    private float attackSpeedMod = 1.0f;
    private float scale = 1.0f;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void load(final PathfinderMob mob, final double p_i1636_2_, final boolean p_i1636_4_, final CallbackInfo ci) {
        final BossCapability cap = ((BossCapEntity) mob).getBossCap();
        if (cap != null && cap.isBoss()) {
            attackSpeedMod = cap.getBossType().getCustomAttributeValueOrDefault(CustomAttributes.ATTACK_SPEED, 1.0f);
            scale = cap.getBossType().getVisualScale();
        }
    }

    @Inject(method = "checkAndPerformAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;resetAttackCooldown()V", shift = At.Shift.AFTER))
    private void resetAttackCooldown(final LivingEntity p_190102_1_, final double p_190102_2_, final CallbackInfo ci) {
        ticksUntilNextAttack = (int) (20 / attackSpeedMod);
    }

    @Inject(method = "getAttackReachSqr", at = @At("HEAD"), cancellable = true)
    private void getAttackReachSqr(final LivingEntity entity, final CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue((double) (mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + entity.getBbWidth()) * scale);
    }
}
