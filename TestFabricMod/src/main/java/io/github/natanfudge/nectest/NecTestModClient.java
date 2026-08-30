package io.github.natanfudge.nectest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class NecTestModClient implements ClientModInitializer {
    private static final KeyMapping tickKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.nec_test.crash",
            GLFW.GLFW_KEY_LEFT_BRACKET,
            KeyMapping.Category.DEBUG
    ));

    private static final KeyMapping localeKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.nec_test.crash_locale",
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            KeyMapping.Category.DEBUG
    ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tickKeyBinding.consumeClick()) {
                var cause = new NecTestCrash("Test Reported Game Loop Crash");
                throw new ReportedException(CrashReport.forThrowable(cause, "Test reported client crash"));
            }
            if (localeKeyBinding.consumeClick()) {
                throw new NecTestCrash("Test Unreported Game Loop Crash");
            }
        });
    }
}
