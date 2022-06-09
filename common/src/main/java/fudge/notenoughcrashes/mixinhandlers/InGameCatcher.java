package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NecConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.StateManager;
import fudge.notenoughcrashes.gui.CrashScreen;
import fudge.notenoughcrashes.patches.MinecraftClientAccess;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import fudge.notenoughcrashes.utils.GlUtil;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.DummyRecorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;

public class InGameCatcher {
    private static final Logger LOGGER = LogManager.getLogger(NotEnoughCrashes.NAME + " In Game Crashes");

    private static int clientCrashCount = 0;
    private static int serverCrashCount = 0;

    public static void handleClientCrash(CrashReport report) {
        clientCrashCount++;
        addInfoToCrash(report);

        resetStates();
        boolean reported = report.getCause() instanceof CrashException;
        LOGGER.fatal(reported ? "Reported" : "Unreported" + " exception thrown!", report.getCause());
        displayCrashScreen(report, clientCrashCount, true);
        // Continue game loop
        getClient().run();
    }

    private static void resetStates() {
        GlUtil.resetState();
        StateManager.resetStates();
        resetModState();
        resetCriticalGameState();
    }

    public static void cleanupBeforeMinecraft(Queue<Runnable> renderTaskQueue) {
        if (getClient().getNetworkHandler() != null) {
            // Fix: Close the connection to avoid receiving packets from old server
            // when playing in another world (MC-128953)
            getClient().getNetworkHandler().getConnection().disconnect(Text.of(String.format("[%s] Client crashed", NotEnoughCrashes.NAME)));
        }

        getClient().disconnect(new MessageScreen(NecLocalization.translatedText("menu.savingLevel")));

        renderTaskQueue.clear(); // Fix: method_1550(null, ...) only clears when integrated server is running
    }

    // Sometimes the game fails to reset this so we make sure it happens ourselves
    private static void resetCriticalGameState() {
        MinecraftClient client = getClient();
        // Turn off profiler because it will crash the game if the world is closed
        if (((MinecraftClientAccess) client).getRecorder().isActive()) {
            client.toggleDebugProfiler(null);
            ((MinecraftClientAccess) client).setRecorder(DummyRecorder.INSTANCE);
        }
        client.player = null;
        client.world = null;
    }

    private static void resetModState() {
//        NotEnoughCrashesApi.permanentDisposers.forEach(Runnable::run);
//        NotEnoughCrashesApi.oneTimeDisposers.forEach(Runnable::run);
//        NotEnoughCrashesApi.oneTimeDisposers.clear();
    }

    public static void handleServerCrash(CrashReport report) {
        serverCrashCount++;
        addInfoToCrash(report);
        displayCrashScreen(report, serverCrashCount, false);
    }

    private static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    public static void addInfoToCrash(CrashReport report) {
        report.getSystemDetailsSection().addSection("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getSystemDetailsSection().addSection("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }

    private static void displayCrashScreen(CrashReport report, int crashCount, boolean clientCrash) {
        try {
            if (EntryPointCatcher.crashedDuringStartup()) {
                throw new IllegalStateException("Could not initialize startup crash screen");
            }
            if (crashCount > NecConfig.instance().crashLimit) {
                throw new IllegalStateException("The game has crashed an excessive amount of times");
            }

            CrashUtils.outputReport(report, clientCrash);

            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            getClient().options.debugEnabled = false;
            getClient().inGameHud.getChatHud().clear(true);

            // Display the crash screen
            getClient().setScreen(new CrashScreen(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            MinecraftClient.printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }
}
