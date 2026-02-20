package com.brutalbosses;

import com.brutalbosses.command.CommandSpawnBoss;
import com.brutalbosses.compat.Compat;
import com.brutalbosses.config.CommonConfiguration;
import com.brutalbosses.event.EventHandler;
import com.brutalbosses.event.ModEventHandler;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.BossTypeSyncMessage;
import com.brutalbosses.network.VanillaParticleMessage;
import com.cupboard.config.CupboardConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.brutalbosses.BrutalBosses.MODID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
public class BrutalBosses
{
    public static final String                              MODID  = "brutalbosses";
    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MODID, new CommonConfiguration());
    public static       Random                              rand   = new Random();

    private final IEventBus modEventBus;

    public BrutalBosses(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.register(ModEventHandler.class);
        NeoForge.EVENT_BUS.register(EventHandler.class);
        NeoForge.EVENT_BUS.addListener(this::onCommandsRegister);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::register);
        this.modEventBus = modEventBus;

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            BrutalBossesClient.onInitializeClient(modEventBus);
        }
    }

    @SubscribeEvent
    public void register(final RegisterPayloadHandlersEvent event)
    {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(BossCapMessage.TYPE,
          StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new BossCapMessage().read(byteBuf)),
          (msg, context) -> msg.handle(context.player()));
        registrar.playToClient(BossOverlayMessage.TYPE,
          StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new BossOverlayMessage().read(byteBuf)),
          (msg, context) -> msg.handle(context.player()));
        registrar.playToClient(BossTypeSyncMessage.TYPE,
          StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new BossTypeSyncMessage().read(byteBuf)),
          (msg, context) -> msg.handle(context.player()));
        registrar.playToClient(VanillaParticleMessage.TYPE,
          StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new VanillaParticleMessage().read(byteBuf)),
          (msg, context) -> msg.handle(context.player()));
    }

    private static Runnable catchErrorsFor(final Runnable runnable)
    {
        return () -> {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                BrutalBosses.LOGGER.warn("error during packet:", e);
            }
        };
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        Compat.initCompat();
        LOGGER.info(MODID + " mod initialized");
    }

    public void onCommandsRegister(final RegisterCommandsEvent event)
    {
        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(MODID);
        event.getDispatcher().register(root.then(new CommandSpawnBoss().build()));
    }
}
