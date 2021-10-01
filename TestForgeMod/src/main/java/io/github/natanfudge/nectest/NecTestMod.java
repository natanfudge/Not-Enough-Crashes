package io.github.natanfudge.nectest;

import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("nec_testmod")
public class NecTestMod {
    public static String getTestMode() {
        String value = System.getProperty("nec_test");
        if (value == null) {
            return "none";
        } else return value;
    }


    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static final KeyBinding key = new KeyBinding("key.nec_test.crash", GLFW.GLFW_KEY_LEFT_BRACKET, "category.nec_test");

    public NecTestMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        ClientRegistry.registerKeyBinding(key);

    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents {

        //TODO: this is the wrong event subscription
        @SubscribeEvent
        public static void onClientTick(final TickEvent.ClientTickEvent event) {
            if (key.isPressed()) {
                throw new NecTestCrash("test crash");
            }
        }
    }


}
