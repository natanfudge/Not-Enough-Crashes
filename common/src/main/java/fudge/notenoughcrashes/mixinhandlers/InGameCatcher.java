package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.gui.CrashScreen;
import fudge.notenoughcrashes.patches.MinecraftClientAccess;
import fudge.notenoughcrashes.patches.CrashReportResetter;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import fudge.notenoughcrashes.utils.GlUtil;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.CrashReport;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;

public class InGameCatcher {
    private static final Logger LOGGER = LogManager.getLogger(NotEnoughCrashes.NAME + " In Game Crashes");

    private static int clientCrashCount = 0;
    private static int serverCrashCount = 0;
    public static boolean crashScreenActive = false;

    public static void handleClientCrash(CrashReport report) {
        clientCrashCount++;
        addInfoToCrash(report);

        var world = crashingWorld();
        resetStates(report);
        displayCrashScreen(report, clientCrashCount, true, world);
    }

    private static void resetStates(CrashReport report) {
        ((CrashReportResetter) getClient()).nec$clearHandledCrash(report);
        // Custom overlays may render during frame presentation, so discard the failed screen first.
        getClient().mouseHandler.releaseMouse();
        KeyMapping.releaseAll();
        getClient().gui.setScreen(null);
        ((MinecraftClientAccess) getClient()).releaseCrashedFrame();
        GlUtil.resetState();
        cleanupBeforeMinecraft();
        resetCriticalGameState();
    }

    public static void cleanupBeforeMinecraft() {
        if (getClient().getConnection() != null) {
            // Fix: Close the connection to avoid receiving packets from old server
            // when playing in another world (MC-128953)
            getClient().getConnection().getConnection().disconnect(Component.literal(String.format("[%s] Client crashed", NotEnoughCrashes.NAME)));
        }

        getClient().disconnect(new GenericMessageScreen(NecLocalization.translatedText("menu.savingLevel")), false);

    }

    // Sometimes the game fails to reset this so we make sure it happens ourselves
    private static void resetCriticalGameState() {
        Minecraft client = getClient();
        // Turn off profiler because it will crash the game if the world is closed
        if (((MinecraftClientAccess) client).getRecorder().isRecording()) {
            ((MinecraftClientAccess) client).getRecorder().cancel();
            ((MinecraftClientAccess) client).setRecorder(InactiveMetricsRecorder.INSTANCE);
        }
        client.player = null;
        client.level = null;

        var server = client.getSingleplayerServer();
        if (server != null) server.halt(true);
    }

    private static java.nio.file.Path crashingWorld() {
        var server = getClient().getSingleplayerServer();
        return server == null ? null : server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize();
    }

    public static void handleServerCrash(CrashReport report) {
        serverCrashCount++;
        addInfoToCrash(report);
        var world = crashingWorld();
        resetStates(report);
        displayCrashScreen(report, serverCrashCount, false, world);
    }

    private static Minecraft getClient() {
        return Minecraft.getInstance();
    }

    public static void addInfoToCrash(CrashReport report) {
        report.getSystemReport().setDetail("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getSystemReport().setDetail("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }

    public static void displayCrashScreen(CrashReport report, int crashCount, boolean clientCrash) {
        displayCrashScreen(report, crashCount, clientCrash, null);
    }

    private static void displayCrashScreen(CrashReport report, int crashCount, boolean clientCrash, java.nio.file.Path world) {
        crashScreenActive = true;
        try {
            if (EntryPointCatcher.crashedDuringStartup()) {
                throw new IllegalStateException("Could not initialize startup crash screen");
            }
            if (crashCount > NecConfig.getCurrent().crashLimit()) {
                throw new IllegalStateException("The game has crashed an excessive amount of times");
            }

            CrashUtils.outputReport(report, clientCrash);

            // Display the crash screen
            getClient().setScreenAndShow(new CrashScreen(report, world));
        } catch (Throwable t) {
            crashScreenActive = false;
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            Minecraft.saveReport(getClient().gameDirectory, report);
            System.exit(report.getSaveFile() != null ? -1 : -2);
        }
    }
}
