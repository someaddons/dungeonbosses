package com.brutalbosses.entity.capability;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BossCapability implements ICapabilitySerializable<INBT>
{
    @CapabilityInject(BossCapability.class)
    public static final Capability<BossCapability> BOSS_CAP = null;

    private BossType                     bossEntry = null;
    private Entity                       entity    = null;
    private ResourceLocation             lootTable = null;
    private BlockPos                     spawnPos  = BlockPos.ZERO;
    private LazyOptional<BossCapability> optional  = LazyOptional.of(() -> this);

    private final static String KEY         = "bbosspath";
    private final static String NAMESPACE   = "bbossnamesp";
    private final static String LTKEY       = "bbossltk";
    private final static String LTNAMESPACE = "bbossltn";
    private final static String XSPAWN      = "spX";
    private final static String YSPAWN      = "spY";
    private final static String ZSPAWN      = "spZ";
    private final static String SHOWBOSSBAR = "shb";

    public static ResourceLocation ID = new ResourceLocation(BrutalBosses.MODID, "bosscap");

    public BossCapability(final Entity entity)
    {
        this.entity = entity;
    }

    public BossCapability()
    {
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(
      @Nonnull final Capability<T> cap, @Nullable final Direction side)
    {
        if (cap != BOSS_CAP)
        {
            return LazyOptional.empty();
        }

        return optional.cast();
    }

    @Override
    public INBT serializeNBT()
    {
        if (bossEntry == null)
        {
            return new CompoundNBT();
        }

        final CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putString(NAMESPACE, bossEntry.getID().getNamespace());
        compoundNBT.putString(KEY, bossEntry.getID().getPath());

        if (lootTable != null)
        {
            compoundNBT.putString(LTKEY, lootTable.getNamespace());
            compoundNBT.putString(LTNAMESPACE, lootTable.getPath());
        }

        if (spawnPos != BlockPos.ZERO)
        {
            compoundNBT.putInt(XSPAWN, spawnPos.getX());
            compoundNBT.putInt(YSPAWN, spawnPos.getY());
            compoundNBT.putInt(ZSPAWN, spawnPos.getZ());
        }

        compoundNBT.putBoolean(SHOWBOSSBAR, bossEntry.showBossBar());

        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final INBT nbt)
    {
        if (nbt == null)
        {
            return;
        }

        final CompoundNBT compoundNBT = (CompoundNBT) nbt;

        if (!compoundNBT.contains(NAMESPACE) || !compoundNBT.contains(KEY))
        {
            return;
        }

        final String nameSpace = compoundNBT.getString(NAMESPACE);
        final String path = compoundNBT.getString(KEY);

        final ResourceLocation id = new ResourceLocation(nameSpace, path);

        if (compoundNBT.contains(LTKEY) && compoundNBT.contains(LTNAMESPACE))
        {
            lootTable = new ResourceLocation(compoundNBT.get(LTKEY).getAsString(), compoundNBT.get(LTNAMESPACE).getAsString());
        }

        if (compoundNBT.contains(XSPAWN))
        {
            spawnPos = new BlockPos(compoundNBT.getInt(XSPAWN), compoundNBT.getInt(YSPAWN), compoundNBT.getInt(ZSPAWN));
        }

        bossEntry = BossTypeManager.instance.bosses.get(id);
        if (bossEntry == null)
        {
            BrutalBosses.LOGGER.warn("Could not find boss for id:" + id);
        }
        else
        {
            if (!entity.level.isClientSide())
            {
                bossEntry.initForEntity((LivingEntity) entity);
            }
            else
            {
                bossEntry.initForClientEntity((LivingEntity) entity);
            }
        }

        bossEntry.setBossBar(compoundNBT.getBoolean(SHOWBOSSBAR));
    }

    public void setBossType(final BossType bossEntry)
    {
        this.bossEntry = bossEntry;
    }

    public BlockPos getSpawnPos()
    {
        return spawnPos;
    }

    public void setSpawnPos(final BlockPos pos)
    {
        spawnPos = pos;
    }

    public ResourceLocation getLootTable()
    {
        return lootTable;
    }

    public void setLootTable(final ResourceLocation lootTable)
    {
        this.lootTable = lootTable;
    }

    public boolean isBoss()
    {
        return bossEntry != null;
    }

    public Entity getEntity()
    {
        return entity;
    }

    public BossType getBossType()
    {
        return bossEntry;
    }
}
