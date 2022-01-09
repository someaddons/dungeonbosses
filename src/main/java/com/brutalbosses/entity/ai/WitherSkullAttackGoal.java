package com.brutalbosses.entity.ai;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.ResourceLocation;

public class WitherSkullAttackGoal extends SimpleRangedAttackGoal
{
    public static final ResourceLocation ID = new ResourceLocation("brutalbosses:shootwitherskulls");

    public WitherSkullAttackGoal(final MobEntity mob, final IAIParams params)
    {
        super(mob, params);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    protected ProjectileEntity createProjectile()
    {
        WitherSkullEntity witherskullentity = new WitherSkullEntity(mob.level, mob, 0, 0, 0);
        return witherskullentity;
    }

    @Override
    protected void doRangedAttack(final ProjectileEntity projectileEntity, final LivingEntity target)
    {
        projectileEntity.remove();

        double xDiff = target.getX() - projectileEntity.getX();
        double yDiff = target.getY(0.5D) - projectileEntity.getY();
        double zDiff = target.getZ() - projectileEntity.getZ();


        final WitherSkullEntity witherskullentity = new WitherSkullEntity(mob.level, mob, xDiff, yDiff, zDiff);
        witherskullentity.setOwner(mob);
        if (((WitherSkullParams) params).dangerous)
        {
            witherskullentity.setDangerous(true);
        }
        witherskullentity.setPos(projectileEntity.getX(), projectileEntity.getY(), projectileEntity.getZ());
        mob.level.addFreshEntity(witherskullentity);
    }

    public static class WitherSkullParams extends RangedParams
    {
        private boolean dangerous = false;

        public WitherSkullParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String DANGEROUS = "dangerous";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);
            if (jsonElement.has(DANGEROUS))
            {
                dangerous = jsonElement.get(DANGEROUS).getAsBoolean();
            }
            return this;
        }
    }
}
