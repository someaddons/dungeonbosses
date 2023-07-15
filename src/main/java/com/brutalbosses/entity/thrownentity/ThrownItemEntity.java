package com.brutalbosses.entity.thrownentity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ModEntities;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Custom thrown item entity
 */
public class ThrownItemEntity extends ThrowableItemProjectile
{
    public static  ResourceLocation          ID           = new ResourceLocation(BrutalBosses.MOD_ID, "thrownitem");
    private static EntityDataAccessor<Float> DATA_VSSCALE = SynchedEntityData.defineId(ThrownItemEntity.class, EntityDataSerializers.FLOAT);

    /**
     * Scale of the entity, synced
     */
    private float scale = 1.0f;

    public ThrownItemEntity(EntityType<Entity> type, final Level world)
    {
        super((EntityType<ThrownItemEntity>)(Object) type, world);
        noCulling = true;
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.getEntityData().define(DATA_VSSCALE, 1.0f);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter)
    {
        super.onSyncedDataUpdated(dataParameter);
        if (dataParameter == DATA_VSSCALE)
        {
            this.scale = getEntityData().get(DATA_VSSCALE);
        }
    }

    public ThrownItemEntity(final Level level, final Mob mob)
    {
        super((EntityType<? extends ThrowableItemProjectile>) (Object)ModEntities.THROWN_ITEMC, mob, level);
        noCulling = true;
    }

    @Override
    protected Item getDefaultItem()
    {
        return Items.ACACIA_DOOR;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public float getScale()
    {
        return scale;
    }

    /**
     * Sets the entity visual scale and dimension
     *
     * @param scale
     */
    public void setScale(final float scale)
    {
        this.scale = scale;
        dimensions = ModEntities.THROWN_ITEMC.getDimensions().scale(scale);
        getEntityData().set(DATA_VSSCALE, scale);
    }
}
