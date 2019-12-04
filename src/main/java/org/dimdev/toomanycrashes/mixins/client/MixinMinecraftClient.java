package org.dimdev.toomanycrashes.mixins.client;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
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

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.CloudRenderMode;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.ClientResourcePackProfile;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.thread.ReentrantThreadExecutor;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> implements PatchedClient {

    @Shadow
    public static byte[] memoryReservedForCrash;
    @Shadow
    @Final
    public static Identifier DEFAULT_TEXT_RENDERER_ID;
    @Shadow
    @Final
    public static boolean IS_SYSTEM_MAC;
    // @formatter:off
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    public GameOptions options;
    @Shadow
    public InGameHud inGameHud;
    @Shadow
    public Screen currentScreen;
    @Shadow
    public TextureManager textureManager;
    @Shadow
    public TextRenderer textRenderer;
    //    @Shadow public abstract void method_1550(ClientWorld world, Gui loadingGui);
    @Shadow
    public Window window;
    @Shadow
    public Mouse mouse;
    @Shadow
    @Final
    public File runDirectory;
    @Shadow
    public Keyboard keyboard;
    @Shadow
    volatile boolean running;
    @Shadow
    private CrashReport crashReport;
    @Shadow
    private int attackCooldown;
    @Shadow
    private Framebuffer framebuffer;
    @Shadow
    private ReloadableResourceManager resourceManager;
    @Shadow
    private SoundManager soundManager;
    @Shadow
    private LanguageManager languageManager;
    @Shadow
    private FontManager fontManager;
    @Shadow
    @Final
    private ResourcePackManager<ClientResourcePackProfile> resourcePackManager;
    @Shadow
    @Final
    private Queue<Runnable> renderTaskQueue;

    @Final
    @Shadow public  GameRenderer gameRenderer;

    @Shadow @Final private  RenderTickCounter renderTickCounter;

    private int clientCrashCount = 0;
    private int serverCrashCount = 0;




    public MixinMinecraftClient(String string_1) {
        super(string_1);
    }

    @Shadow
    public void openScreen(Screen gui) {
    }
    // @formatter:on

    @Shadow
    public CrashReport addDetailsToCrashReport(CrashReport report) {
        return null;
    }

    @Shadow
    public void close() {
    }

    @Shadow
    public abstract ClientPlayNetworkHandler getNetworkHandler();


    @Shadow
    protected abstract void render(boolean boolean_1);

    @Shadow
    public abstract CompletableFuture<Void> reloadResources();

    @Shadow
    public abstract boolean forcesUnicodeFont();

    @Shadow
    public abstract void stop();

    @Shadow
    public abstract void disconnect(Screen screen);

    /**
     * @author runemoro
     * @reason Allows the player to choose to return to the title screen after a crash, or get
     * a pasteable link to the crash report on paste.dimdev.org.
     */
    @Overwrite
    public void run() {
        while (running) {
            if ( crashReport == null) {
                try {
                    render(true);
                } catch (CrashException e) {
                    clientCrashCount++;
                    addDetailsToCrashReport(e.getReport());
                    addInfoToCrash(e.getReport());
                    resetGameState();
                    LOGGER.fatal("Reported exception thrown!", e);
                    displayCrashScreen(e.getReport());
                } catch (Throwable e) {
                    clientCrashCount++;
                    CrashReport report = new CrashReport("Unexpected error", e);

                    addDetailsToCrashReport(report);
                    addInfoToCrash(report);
                    resetGameState();
                    LOGGER.fatal("Unreported exception thrown!", e);
                    openScreen(new CrashScreen(report));
//                    displayCrashScreen(report);
                }
            } else {
                serverCrashCount++;
                addInfoToCrash(crashReport);
                resetGameState();
                displayCrashScreen(crashReport);

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
            running = true;
            runGUILoop(new InitErrorScreen(report));
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void runGUILoop(Screen screen) {
        openScreen(screen);

        while (running && currentScreen != null && !(currentScreen instanceof TitleScreen)) {
//            this.window.setPhase("Pre Artificial render");
//
//            this.mouse.updateMouse();
//            this.window.setPhase("Render");
////            this.soundManager.updateListenerPosition(this.gameRenderer.getCamera());
//            RenderSystem.pushMatrix();
//            RenderSystem.clear(16640, IS_SYSTEM_MAC);
//            this.framebuffer.beginWrite(true);
//            BackgroundRenderer.method_23792();
//            RenderSystem.enableTexture();
//
//            currentScreen.render(
//                            (int) (mouse.getX() * window.getScaledWidth() / window.getWidth()),
//                            (int) (mouse.getY() * window.getScaledHeight() / window.getHeight()),
//                            0
//            );
//
//            long startTime = Util.getMeasuringTimeNano();
//
//
////            if (!this.skipGameRender) {
//                this.gameRenderer.render(this.renderTickCounter.tickDelta, startTime, false);
////            }
//
//            this.framebuffer.endWrite();
//            RenderSystem.popMatrix();
//            RenderSystem.pushMatrix();
//            this.framebuffer.draw(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
//            RenderSystem.popMatrix();
//            this.window.setFullscreen();
//
//            Thread.yield();
//            this.window.setPhase("Post Artificial render");




            window.setPhase("TooManyCrashes GUI Loop");
            if (GLX._shouldClose(window)) {
                stop();
            }

            attackCooldown = 10000;
            currentScreen.tick();

            mouse.updateMouse();

            RenderSystem.pushMatrix();
            RenderSystem.clear(16640, IS_SYSTEM_MAC);
            framebuffer.beginWrite(true);
            RenderSystem.enableTexture();

            RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();

            RenderSystem.clear(256, IS_SYSTEM_MAC);

            unknownMethodOfImportantGl();

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
            unknownMethodOfImportantGl();
            RenderSystem.popMatrix();
            window.setFullscreen();
            Thread.yield();
        }
    }

    private void unknownMethodOfImportantGl() {
        RenderSystem.clear(256, IS_SYSTEM_MAC);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
    }

    public void displayCrashScreen(CrashReport report) {
        try {
            CrashUtils.outputReport(report);

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
    public static void printCrashReport(CrashReport report) {
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
