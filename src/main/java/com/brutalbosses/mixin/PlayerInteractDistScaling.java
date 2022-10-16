package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class PlayerInteractDistScaling
{
    @Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    public double getAdjustedDistance(final ServerPlayer player, final Entity entity)
    {
        final BossCapability cap = entity.getCapability(BossCapability.BOSS_CAP).orElse(null);

        if (cap != null && cap.isBoss())
        {
            return player.distanceToSqr(entity) / Math.max(0.1, cap.getBossType().getVisualScale());
        }

        return player.distanceToSqr(entity);
    }
}
