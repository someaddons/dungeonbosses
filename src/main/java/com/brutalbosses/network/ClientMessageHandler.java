package com.brutalbosses.network;

import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.event.ClientEventHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ClientMessageHandler
{
    private static final float WIDTH  = 0.8f;
    private static final float HEIGHT = 2;

    public static void handle(VanillaParticleMessage particleMessage, Minecraft client)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        spawnParticles(particleMessage.type, world, particleMessage.x, particleMessage.y, particleMessage.z);
    }

    /**
     * Spawns the given particle randomly around the position.
     *
     * @param particleType praticle to spawn
     * @param world        world to use
     * @param x            x pos
     * @param y            y pos
     * @param z            z pos
     */
    private static void spawnParticles(SimpleParticleType particleType, Level world, double x, double y, double z)
    {
        final Random rand = new Random();
        for (int i = 0; i < 5; ++i)
        {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
              x + (rand.nextFloat() * WIDTH * 2.0F) - WIDTH,
              y + 1.0D + (rand.nextFloat() * HEIGHT),
              z + (rand.nextFloat() * WIDTH * 2.0F) - WIDTH,
              d0,
              d1,
              d2);
        }
    }

    public static void handle(BossCapMessage bossCapMessage, Minecraft client)
    {
        final Entity entity = client.player.level().getEntity(bossCapMessage.entityID);
        if (entity instanceof BossCapEntity)
        {
            ((BossCapEntity) entity).setBossCap(new BossCapability(entity));
            ((BossCapEntity) entity).getBossCap().deserializeNBT(bossCapMessage.nbt);
        }
    }

    public static void handle(BossOverlayMessage message, Minecraft client)
    {
        final Entity entity = client.player.level().getEntity(message.entityID);
        if (entity != null)
        {
            ClientEventHandler.checkEntity(entity);
        }
    }

    public static void handle(BossTypeSyncMessage message, Minecraft client)
    {
        final ImmutableMap.Builder<ResourceLocation, BossType> bossTypesImm = ImmutableMap.<ResourceLocation, BossType>builder();
        for (final BossType type : message.bossTypes)
        {
            bossTypesImm.put(type.getID(), type);
        }

        BossTypeManager.instance.bosses = bossTypesImm.build();
    }
}
