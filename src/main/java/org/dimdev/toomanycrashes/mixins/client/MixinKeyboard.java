package org.dimdev.toomanycrashes.mixins.client;

import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.dimdev.toomanycrashes.PatchedIntegratedServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {

    @Shadow @Final private MinecraftClient client;
    @Shadow private long debugCrashStartTime;

    /**
     * @reason Replaces the vanilla F3 + C logic to immediately crash rather than requiring
     * that the buttons are pressed for 6 seconds and add more crash types:
     * F3 + C - Client crash
     * Ctrl + F3 + C - GL illegal access crash
     * Alt + F3 + C - Integrated server crash
     * Shift + F3 + C - Scheduled client task exception
     * Alt + Shift + F3 + C - Scheduled server task exception
     * <p>
     * Note: Left Shift + F3 + C doesn't work on most keyboards, see http://keyboardchecker.com/
     * Use the right shift instead.
     */
    @Overwrite
    public void pollDebugCrash() {
        if (debugCrashStartTime > 0L && System.currentTimeMillis() - debugCrashStartTime >= 0) {
            // TODO: if the client crashes between F3 + C is pressed and pollDebugCrash is called,
            //       debugCrashStartTime isn't reset and the client will crash again when joining
            //       a world.
            debugCrashStartTime = -1;

            if (Screen.hasControlDown()) {
                GlfwUtil.method_15973();
            } else if (Screen.hasShiftDown()) {
                if (Screen.hasAltDown()) {
                    if (client.isIntegratedServerRunning()) {
                        client.getServer().execute(() -> {
                            throw new CrashException(new CrashReport("Manually triggered server-side scheduled task exception", new Throwable()));
                        });
                    }
                } else {
                    client.execute(ListenableFutureTask.create(() -> {
                        throw new CrashException(new CrashReport("Manually triggered client-side scheduled task exception", new Throwable()));
                    }));
                }
            } else {
                if (Screen.hasAltDown()) {
                    if (client.isIntegratedServerRunning()) {
                        ((PatchedIntegratedServer) client.getServer()).setCrashNextTick();
                    }
                } else {
                    throw new CrashException(new CrashReport("Manually triggered client-side debug crash", new Throwable()));
                }
            }
        }
    }
}
