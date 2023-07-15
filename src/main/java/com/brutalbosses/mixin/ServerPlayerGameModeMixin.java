package com.brutalbosses.mixin;

import com.brutalbosses.event.EventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.level.ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void brutalbosses$useItemOn(final ServerPlayer serverPlayer, final Level level, final ItemStack itemStack, final InteractionHand interactionHand, final BlockHitResult blockHitResult, final CallbackInfoReturnable<InteractionResult> cir) {
        EventHandler.onPlayerInteract(serverPlayer, blockHitResult.getBlockPos(), cir);
    }
}
