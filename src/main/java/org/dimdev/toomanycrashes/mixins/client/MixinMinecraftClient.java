package org.dimdev.toomanycrashes.mixins.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.audio.SoundLoader;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.font.FontRendererManager;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.gui.CloseWorldGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.MainMenuGui;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.ClientResourcePackContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePackContainerManager;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.ThreadTaskQueue;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.Logger;
import org.dimdev.toomanycrashes.CrashScreenGui;
import org.dimdev.toomanycrashes.CrashUtils;
import org.dimdev.toomanycrashes.InitErrorScreenGui;
import org.dimdev.toomanycrashes.StateManager;
import org.dimdev.utils.GlUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class MixinMinecraftClient extends ThreadTaskQueue<Runnable> {
    // @formatter:off
    @Shadow @Final private static Logger LOGGER;
    @Shadow volatile boolean isRunning;
    @Shadow private boolean crashed;
    @Shadow private CrashReport crashReport;
    @Shadow public static byte[] memoryReservedForCrash;
    @Shadow public GameOptions options;
    @Shadow public InGameHud hudInGame;
    @Shadow public Gui currentGui;
    @Shadow public TextureManager textureManager;
    @Shadow public FontRenderer fontRenderer;
    @Shadow private int attackCooldown;
    @Shadow private GlFramebuffer framebuffer;
    @Shadow private ReloadableResourceManager resourceManager;
    @Shadow private SoundLoader soundLoader;
    @Shadow private LanguageManager languageManager;
    @Shadow private void init() {}
    @Shadow public void openGui(Gui gui) {}
    @Shadow public CrashReport populateCrashReport(CrashReport report) { return null; }
    @Shadow public void stop() {}
    @Shadow public abstract ClientPlayNetworkHandler getNetworkHandler();
    @Shadow public abstract void updateDisplay(boolean respectFramerateLimit);
    @Shadow protected abstract void render(boolean boolean_1);
    @Shadow public abstract void method_1550(ClientWorld world, Gui loadingGui);
    @Shadow public Window window;
    @Shadow private FontRendererManager fontManager;
    @Shadow public abstract void reloadResources();
    @Shadow public Mouse mouse;
    @Shadow public abstract boolean forcesUnicodeFont();
    @Shadow @Final public static Identifier defaultFontRendererId;
    @Shadow public abstract void stopThread();
    // @formatter:on

    @Shadow @Final public static boolean isSystemMac;
    @Shadow @Final public File runDirectory;
    @Shadow public Keyboard keyboard;
    @Shadow @Final private ResourcePackContainerManager<ClientResourcePackContainer> resourcePackContainerManager;
    private int clientCrashCount = 0;
    private int serverCrashCount = 0;

    /**
     * @reason Allows the player to choose to return to the title screen after a crash, or get
     * a pasteable link to the crash report on paste.dimdev.org.
     */
    @Overwrite
    public void start() {
        isRunning = true;

        try {
            init();
        } catch (Throwable throwable) {
            // TODO: Error screen for crashes during Bootstrap.initialize() too
            CrashReport report = CrashReport.create(throwable, "Initializing game");
            report.addElement("Initialization");
            displayInitErrorScreen(populateCrashReport(report));
            return;
        }

        try {
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
        } finally {
            stop();
        }
    }

    public void addInfoToCrash(CrashReport report) {
        report.getSystemDetailsSection().add("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getSystemDetailsSection().add("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }

    public void displayInitErrorScreen(CrashReport report) {
        CrashUtils.outputReport(report);

        try {
            GlUtil.resetState();
            isRunning = true;
            runGUILoop(new InitErrorScreenGui(report));
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void runGUILoop(Gui screen) {
        openGui(screen);

        while (isRunning && currentGui != null && !(currentGui instanceof MainMenuGui)) {
            window.setPhase("TooManyCrashes GUI Loop");
            if (GLX.shouldClose(window)) {
                stopThread();
            }

            attackCooldown = 10000;
            currentGui.update();

            mouse.updateMouse();
            GLX.pollEvents();

            GlStateManager.pushMatrix();
            GlStateManager.clear(16640, isSystemMac);
            framebuffer.beginWrite(true);
            GlStateManager.enableTexture();

            GlStateManager.viewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            window.method_4493(isSystemMac);

            GlStateManager.clear(256, isSystemMac);
            currentGui.draw(
                    (int) (mouse.getX() * window.getScaledWidth() / window.method_4480()),
                    (int) (mouse.getY() * window.getScaledHeight() / window.method_4507()),
                    0
            );

            framebuffer.endWrite();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            framebuffer.draw(window.getWindowWidth(), window.getWindowHeight());
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            window.method_4493(isSystemMac);
            GlStateManager.popMatrix();
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
            hudInGame.getHudChat().clear(true);

            // Display the crash screen
            runGUILoop(new CrashScreenGui(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

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
            } catch (Throwable ignored) {}

            // Reset registered resettables
            StateManager.resetStates();

            // Close the world
            if (getNetworkHandler() != null) {
                // Fix: Close the connection to avoid receiving packets from old server
                // when playing in another world (MC-128953)
                getNetworkHandler().getClientConnection().disconnect(new StringTextComponent("[TooManyCrashes] Client crashed"));
            }

            method_1550(null, new CloseWorldGui(I18n.translate("menu.savingLevel")));
            taskQueue.clear(); // Fix: method_1550(null, ...) only clears when integrated server is running

            // Reset graphics
            GlUtil.resetState();

            // Re-create memory reserve so that future crashes work well too
            if (originalReservedMemorySize != -1) {
                try {
                    memoryReservedForCrash = new byte[originalReservedMemorySize];
                } catch (Throwable ignored) {}
            }

            System.gc();
        } catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
            try {
                StateManager.resetStates();
                GlUtil.resetState();
            } catch (Throwable ignored) {}
        }
    }

    /**
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
