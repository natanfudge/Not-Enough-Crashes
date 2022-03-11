package io.github.natanfudge.nectest;

import net.minecraft.client.option.KeyBinding;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public class NecTestModClient {
    public static final KeyBinding key = new KeyBinding("key.nec_test.crash", GLFW.GLFW_KEY_LEFT_BRACKET, "category.nec_test");


    public static void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(key);
    }
}
