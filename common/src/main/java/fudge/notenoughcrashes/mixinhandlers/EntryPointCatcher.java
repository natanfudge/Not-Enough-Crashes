package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntryPointCatcher {
    private static CrashReport crashReport = null;

    public static boolean crashedDuringStartup() {
        return crashReport != null;
    }

    private static final Logger LOGGER = LogManager.getLogger(NotEnoughCrashes.NAME + " Entry Points");


    public static void handleEntryPointError(Throwable e) {
        crashReport = CrashReport.forThrowable(e, "Initializing game");
        crashReport.addCategory("Initialization");
        Minecraft.getInstance().fillReport(crashReport);
        CrashUtils.outputClientReport(crashReport);

        // Make GL shuttup about any GL error that occurred
        Window.checkGlfwError((integer, stringx) -> {
        });
    }


    public static void displayInitErrorScreen() {
        try {
            Minecraft.getInstance().setScreen(new InitErrorScreen(crashReport));
        } catch (Throwable t) {
            CrashReport additionalReport = CrashReport.forThrowable(t, "Displaying init error screen");
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            CrashUtils.outputClientReport(additionalReport);
            System.exit(additionalReport.getSaveFile() != null ? -1 : -2);
        }
    }

}
