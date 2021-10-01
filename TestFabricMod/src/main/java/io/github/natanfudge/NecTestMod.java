package io.github.natanfudge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class NecTestMod implements ModInitializer {
    public static String getTestMode() {
        String value = System.getProperty("nec_test");
        if (value == null) {
            return "none";
        } else return value;
    }

    private static KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nec_test.crash", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_LEFT_BRACKET, // The keycode of the key
            "category.nec_test" // The translation key of the keybinding's category.
    ));
    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed()) {
                throw new NecTestCrash("Test Game Loop Crash");
            }
        });
    }
}

