package io.github.natanfudge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NecTestMod implements ModInitializer {
    public static String getTestMode() {
        Path configDig = FabricLoader.getInstance().getConfigDir();
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

    private static final KeyBinding tickKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nec_test.crash", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_LEFT_BRACKET, // The keycode of the key
            "category.nec_test" // The translation key of the keybinding's category.
    ));

    private static final KeyBinding localeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nec_test.crash_locale", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_RIGHT_BRACKET, // The keycode of the key
            "category.nec_test" // The translation key of the keybinding's category.
    ));

    @Override
    public void onInitialize() {
        if (getTestMode().equals("init_crash")) {
            throw new NecTestCrash("Test Init Crash");
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tickKeyBinding.wasPressed()) {
                throw new NecTestCrash("Test Game Loop Crash");
            }
            if (localeKeyBinding.wasPressed()) {
                throw new NecTestCrash("שלום עולם");
            }
        });
    }
}

