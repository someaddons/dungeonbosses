package com.brutalbosses.data;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

public record PotionData(
    Holder<MobEffect> effect,
    int duration,
    int amplifier,
    @Nullable ParticleType particleType)
{
    public PotionData(Holder<MobEffect> effect, int duration, int amplifier)
    {
        this(effect, duration, amplifier, null);
    }
}
