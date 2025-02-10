package com.brutalbosses.mixin;

import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(AreaEffectCloud.class)
public class AreaEffectCloudMixin
{
    @Shadow
    public LivingEntity owner;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"))
    private boolean skipOwner(final Map instance, final Object o)
    {
        if (!instance.containsKey(o))
        {
            if (owner instanceof Player)
            {
                return true;
            }

            return o == owner;
        }

        return true;
    }
}
