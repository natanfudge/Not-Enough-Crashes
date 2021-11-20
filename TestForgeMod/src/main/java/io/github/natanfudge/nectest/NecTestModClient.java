package io.github.natanfudge.nectest;

import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.lwjgl.glfw.GLFW;

public class NecTestModClient {
    public static final KeyBinding key = new KeyBinding("key.nec_test.crash", GLFW.GLFW_KEY_LEFT_BRACKET, "category.nec_test");


    public static void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(key);
    }
}
