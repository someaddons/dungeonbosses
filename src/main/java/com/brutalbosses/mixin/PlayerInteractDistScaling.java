package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.extensions.IForgePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IForgePlayer.class)
public interface PlayerInteractDistScaling
{
    @Shadow(remap = false)
    public abstract boolean isCloseEnough(final Entity entity, final double dist);

    @Shadow(remap = false)
    public abstract double getAttackRange();

    /**
     * @param entity
     * @param padding
     * @return
     */
    @Overwrite(remap = false)
    default boolean canHit(Entity entity, double padding)
    {
        final BossCapability cap = entity.getCapability(BossCapability.BOSS_CAP).orElse(null);

        if (cap != null && cap.isBoss())
        {
            return isCloseEnough(entity, getAttackRange() * cap.getBossType().getVisualScale() + padding);
        }
        return isCloseEnough(entity, getAttackRange() + padding);
    }
}
