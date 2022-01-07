package com.brutalbosses.entity.thrownentity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Custom thrown item entity
 */
public class ThrownItemEntity extends ProjectileItemEntity
{
    public static  ResourceLocation     ID           = new ResourceLocation(BrutalBosses.MODID, "thrownitem");
    private static DataParameter<Float> DATA_VSSCALE = EntityDataManager.defineId(ProjectileItemEntity.class, DataSerializers.FLOAT);

    /**
     * Scale of the entity, synced
     */
    private float scale = 1.0f;

    public ThrownItemEntity(final EntityType<? extends ProjectileItemEntity> type, final World world)
    {
        super(type, world);
        noCulling = true;
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.getEntityData().define(DATA_VSSCALE, 1.0f);
    }

    public void onSyncedDataUpdated(DataParameter<?> dataParameter)
    {
        super.onSyncedDataUpdated(dataParameter);
        if (dataParameter == DATA_VSSCALE)
        {
            this.scale = getEntityData().get(DATA_VSSCALE);
        }
    }

    public ThrownItemEntity(final World level, final MobEntity mob)
    {
        super(ModEntities.THROWN_ITEMC, mob, level);
        noCulling = true;
    }

    @Override
    protected Item getDefaultItem()
    {
        return Items.ACACIA_DOOR;
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
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
