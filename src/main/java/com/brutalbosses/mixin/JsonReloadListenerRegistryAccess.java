package com.brutalbosses.mixin;

import com.brutalbosses.entity.BossJsonListener;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class JsonReloadListenerRegistryAccess
{
    @Inject(method = "method_58296", at = @At("HEAD"))
    private static void ongetContext(
        final FeatureFlagSet featureFlagSet,
        final Commands.CommandSelection commandSelection,
        final int i,
        final ResourceManager resourceManager,
        final Executor executor,
        final Executor executor2,
        final LayeredRegistryAccess layeredRegistryAccess,
        final CallbackInfoReturnable<CompletionStage> cir)
    {
        BossJsonListener.layeredRegistryAccess = layeredRegistryAccess;
    }
}
