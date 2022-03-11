package io.github.natanfudge.nectest;

import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("nec_testmod")
public class NecTestMod {
    public static String getTestMode() {
        Path configDig = FMLPaths.CONFIGDIR.get();
        Path testModePath = configDig.resolve("nec_test_mode.txt");
        try {
            Files.createDirectories(testModePath.getParent());
            if (!Files.exists(testModePath)) {
                Files.createFile(testModePath);
            }
            return new String(Files.readAllBytes(testModePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public NecTestMod() {
        if (getTestMode().equals("init_crash")) {
            throw new NecTestCrash("Test Init Crash");
        }

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(NecTestModClient::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents {

        //TODO: this is the wrong event subscription
        @SubscribeEvent
        public static void onClientTick(final TickEvent.ClientTickEvent event) {
            if (NecTestModClient.key.isPressed()) {
                throw new NecTestCrash("test crash");
            }
        }
    }


}
