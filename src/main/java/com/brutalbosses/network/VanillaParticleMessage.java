package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Message for vanilla particles around a citizen, in villager-like shape.
 */
public class VanillaParticleMessage implements IMessage
{
    private static final float  WIDTH          = 0.8f;
    private static final float  CITIZEN_HEIGHT = 2;
    /**
     * Citizen Position
     */
    private              double x;
    private              double y;
    private              double z;

    /**
     * Particle id
     */
    private SimpleParticleType type;

    public VanillaParticleMessage() {super();}

    public VanillaParticleMessage(final double x, final double y, final double z, final SimpleParticleType type)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
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
    private void spawnParticles(SimpleParticleType particleType, Level world, double x, double y, double z)
    {
        final Random rand = new Random();
        for (int i = 0; i < 5; ++i)
        {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
              x + (rand.nextFloat() * WIDTH * 2.0F) - WIDTH,
              y + 1.0D + (rand.nextFloat() * CITIZEN_HEIGHT),
              z + (rand.nextFloat() * WIDTH * 2.0F) - WIDTH,
              d0,
              d1,
              d2);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeResourceLocation(this.type.getRegistryName());
    }

    @Override
    public IMessage read(final FriendlyByteBuf buffer)
    {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        this.type = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(buffer.readResourceLocation());
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(final Supplier<NetworkEvent.Context> contextSupplier)
    {
        if (contextSupplier.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
        {
            BrutalBosses.LOGGER.error("Boss capability message sent to the wrong side!", new Exception());
            return;
        }
        final ClientLevel world = Minecraft.getInstance().level;

        spawnParticles(type, world, x, y, z);
    }
}
