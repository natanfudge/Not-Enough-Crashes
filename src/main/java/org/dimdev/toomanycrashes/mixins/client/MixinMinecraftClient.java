package org.dimdev.toomanycrashes.mixins.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.ClientResourcePackContainer;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePackContainerManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.NonBlockingThreadExecutor;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.Logger;
import org.dimdev.toomanycrashes.CrashScreen;
import org.dimdev.toomanycrashes.CrashUtils;
import org.dimdev.toomanycrashes.InitErrorScreen;
import org.dimdev.toomanycrashes.PatchedClient;
import org.dimdev.toomanycrashes.StateManager;
import org.dimdev.utils.GlUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class MixinMinecraftClient extends NonBlockingThreadExecutor<Runnable> implements PatchedClient {

    @Shadow public static byte[] memoryReservedForCrash;
    @Shadow @Final public static Identifier DEFAULT_TEXT_RENDERER_ID;
    @Shadow @Final public static boolean IS_SYSTEM_MAC;
    // @formatter:off
    @Shadow @Final private static Logger LOGGER;
    @Shadow public GameOptions options;
    @Shadow public InGameHud inGameHud;
    @Shadow public Screen currentScreen;
    @Shadow public TextureManager textureManager;
    @Shadow public TextRenderer textRenderer;
//    @Shadow public abstract void method_1550(ClientWorld world, Gui loadingGui);
    @Shadow public Window window;
    @Shadow public Mouse mouse;
    @Shadow @Final public File runDirectory;
    @Shadow public Keyboard keyboard;
    @Shadow volatile boolean isRunning;
    @Shadow private boolean crashed;
    @Shadow private CrashReport crashReport;
    @Shadow private int attackCooldown;
    @Shadow private GlFramebuffer framebuffer;
    @Shadow private ReloadableResourceManager resourceManager;
    @Shadow private SoundManager soundManager;
    @Shadow private LanguageManager languageManager;
    @Shadow private FontManager fontManager;
    @Shadow @Final private ResourcePackContainerManager<ClientResourcePackContainer> resourcePackContainerManager;
    @Shadow @Final private Queue<Runnable> renderTaskQueue;
    private int clientCrashCount = 0;
    private int serverCrashCount = 0;
    public MixinMinecraftClient(String string_1) {
        super(string_1);
    }

    @Shadow private void init() {}

    @Shadow public void openScreen(Screen gui) {}
    // @formatter:on

    @Shadow public CrashReport populateCrashReport(CrashReport report) { return null; }

    @Shadow public void close() {}

    @Shadow public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow public abstract void updateDisplay(boolean respectFramerateLimit);

    @Shadow protected abstract void render(boolean boolean_1);

    @Shadow public abstract CompletableFuture<Void> reloadResources();

    @Shadow public abstract boolean forcesUnicodeFont();

    @Shadow public abstract void stop();

    @Shadow public abstract void disconnect(Screen screen);

    /**
     * @author runemoro
     * @reason Allows the player to choose to return to the title screen after a crash, or get
     * a pasteable link to the crash report on paste.dimdev.org.
     */
    @Overwrite
    public void start() {
        while (isRunning) {
            if (!crashed || crashReport == null) {
                try {
                    render(true);
                } catch (CrashException e) {
                    clientCrashCount++;
                    populateCrashReport(e.getReport());
                    addInfoToCrash(e.getReport());
                    resetGameState();
                    LOGGER.fatal("Reported exception thrown!", e);
                    displayCrashScreen(e.getReport());
                } catch (Throwable e) {
                    clientCrashCount++;
                    CrashReport report = new CrashReport("Unexpected error", e);

                    populateCrashReport(report);
                    addInfoToCrash(report);
                    resetGameState();
                    LOGGER.fatal("Unreported exception thrown!", e);
                    displayCrashScreen(report);
                }
            } else {
                serverCrashCount++;
                addInfoToCrash(crashReport);
                resetGameState();
                displayCrashScreen(crashReport);
                crashed = false;
                crashReport = null;
            }
        }
    }

    public void addInfoToCrash(CrashReport report) {
        report.getSystemDetailsSection().add("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getSystemDetailsSection().add("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }

    @Override
    public void displayInitErrorScreen(CrashReport report) {
        CrashUtils.outputReport(report);

        try {
            GlUtil.resetState();
            isRunning = true;
            runGUILoop(new InitErrorScreen(report));
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void runGUILoop(Screen screen) {
        openScreen(screen);

        while (isRunning && currentScreen != null && !(currentScreen instanceof TitleScreen)) {
            window.setPhase("TooManyCrashes GUI Loop");
            if (GLX._shouldClose(window)) {
                stop();
            }

            attackCooldown = 10000;
            currentScreen.tick();

            mouse.updateMouse();
            GLX._pollEvents();

            RenderSystem.pushMatrix();
            RenderSystem.clear(16640, IS_SYSTEM_MAC);
            framebuffer.beginWrite(true);
            RenderSystem.enableTexture();

            RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            window.method_4493(IS_SYSTEM_MAC);

            RenderSystem.clear(256, IS_SYSTEM_MAC);
            currentScreen.render(
                    (int) (mouse.getX() * window.getScaledWidth() / window.getWidth()),
                    (int) (mouse.getY() * window.getScaledHeight() / window.getHeight()),
                    0
            );

            framebuffer.endWrite();
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            framebuffer.draw(window.getWidth(), window.getHeight());
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            window.method_4493(IS_SYSTEM_MAC);
            RenderSystem.popMatrix();
            updateDisplay(true);
            Thread.yield();
        }
    }

    public void displayCrashScreen(CrashReport report) {
        try {
            CrashUtils.outputReport(report);

            // Reset hasCrashed
            crashed = false;

            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            options.debugEnabled = false;
            inGameHud.getChatHud().clear(true);

            // Display the crash screen
            runGUILoop(new CrashScreen(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    /**
     * @author Runemoro
     * @reason Substitute
     */
    @Overwrite
    public void printCrashReport(CrashReport report) {
        CrashUtils.outputReport(report);
    }

    public void resetGameState() {
        try {
            // Free up memory such that this works properly in case of an OutOfMemoryError
            int originalReservedMemorySize = -1;
            try { // In case another mod actually deletes the memoryReserve field
                if (memoryReservedForCrash != null) {
                    originalReservedMemorySize = memoryReservedForCrash.length;
                    memoryReservedForCrash = new byte[0];
                }
            } catch (Throwable ignored) {
            }

            // Reset registered resettables
            StateManager.resetStates();

            // Close the world
            if (getNetworkHandler() != null) {
                // Fix: Close the connection to avoid receiving packets from old server
                // when playing in another world (MC-128953)
                getNetworkHandler().getConnection().disconnect(new LiteralText("[TooManyCrashes] Client crashed"));
            }

            disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
            renderTaskQueue.clear(); // Fix: method_1550(null, ...) only clears when integrated server is running

            // Reset graphics
            GlUtil.resetState();

            // Re-create memory reserve so that future crashes work well too
            if (originalReservedMemorySize != -1) {
                try {
                    memoryReservedForCrash = new byte[originalReservedMemorySize];
                } catch (Throwable ignored) {
                }
            }

            System.gc();
        } catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
            try {
                StateManager.resetStates();
                GlUtil.resetState();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * @author runemoro
     * @reason Disconnect from the current world and free memory, using a memory reserve
     * to make sure that an OutOfMemory doesn't happen while doing this.
     * <p>
     * Bugs Fixed:
     * - https://bugs.mojang.com/browse/MC-128953
     * - Memory reserve not recreated after out-of memory
     */
    @Overwrite
    public void cleanUpAfterCrash() {
        resetGameState();
    }
}
