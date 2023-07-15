package com.brutalbosses.mixin;

import com.brutalbosses.event.ClientEventHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class PlayerMixin {

    @Inject(method = "tick", at =@At("HEAD"))
    private void onPlayerTick(final CallbackInfo ci)
    {
        ClientEventHandler.onPlayerTick((LocalPlayer)(Object)this);
    }
}
