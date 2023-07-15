package com.brutalbosses.mixin;

import com.brutalbosses.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
     @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
     private void onClearLevel(final Screen screen, final CallbackInfo ci)
     {
         ClientEventHandler.onWorldUnload();
     }

     @Inject(method = "setLevel", at = @At("HEAD"))
     private void onClearLevel(final ClientLevel clientLevel, final CallbackInfo ci)
     {
         ClientEventHandler.onWorldUnload();
     }
}
