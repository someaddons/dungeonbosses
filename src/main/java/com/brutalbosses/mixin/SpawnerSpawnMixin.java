package com.brutalbosses.mixin;

import com.brutalbosses.event.EventHandler;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class SpawnerSpawnMixin
{
    @Inject(method = "finalizeSpawn", at = @At(value = "RETURN"))
    private void onSpawn(
        final ServerLevelAccessor serverLevelAccessor,
        final DifficultyInstance difficultyInstance,
        final MobSpawnType mobSpawnType,
        final SpawnGroupData spawnGroupData,
        final CallbackInfoReturnable<SpawnGroupData> cir)
    {
        EventHandler.onEntitySpawn((Mob) (Object) this, mobSpawnType);
    }
}
