package com.brutalbosses.mixin;

import com.brutalbosses.event.EventHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void brutalbosses$onplayerlogin(final Connection connection, final ServerPlayer serverPlayer, final CallbackInfo ci) {
        EventHandler.onPlayerLogin(serverPlayer);
    }
}
