package com.brutalbosses.mixin;

import com.brutalbosses.entity.CustomEntityRenderData;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.event.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class BossCapMixin extends LivingEntity implements BossCapEntity {

    @Shadow
    public abstract void readAdditionalSaveData(CompoundTag compoundTag);

    @Unique
    private BossCapability bossCap = null;

    protected BossCapMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void brutalbosses$addSaveData(final CompoundTag compoundTag, final CallbackInfo ci) {
        if (bossCap != null) {
            compoundTag.put(BossCapability.ID.toString(), bossCap.serializeNBT());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void brutalbosses$readSaveData(final CompoundTag compoundTag, final CallbackInfo ci) {
        if (compoundTag.contains(BossCapability.ID.toString())) {
            bossCap = new BossCapability((Entity) (Object) this);
            bossCap.deserializeNBT(compoundTag.getCompound(BossCapability.ID.toString()));
        }
    }

    @Override
    public void setBossCap(BossCapability cap) {
        bossCap = cap;
    }

    @Override
    public BossCapability getBossCap() {
        return bossCap;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        if (bossCap != null && bossCap.isBoss()) {
            EventHandler.onTrack(this, serverPlayer);
        }
    }

    @Inject(method = "convertTo", at = @At("HEAD"), cancellable = true)
    private void brutalbosses$onconvert(final EntityType entityType, final boolean bl, final CallbackInfoReturnable cir) {
        if (bossCap != null && bossCap.isBoss()) {
            cir.setReturnValue(null);
        }
    }

    @Override
    public float getPickRadius() {
        if (this instanceof CustomEntityRenderData) {
            return (float) (Math.max(0, (((CustomEntityRenderData) this).getVisualScale() - 1f)) * getBoundingBox().getSize());
        }

        return 0.0F;
    }
}
