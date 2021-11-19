package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntryPointCatcher {
    private static CrashReport crashReport = null;

    public static boolean crashedDuringStartup() {
        return crashReport != null;
    }

    private static final Logger LOGGER = LogManager.getLogger(NotEnoughCrashes.NAME + " Entry Points");


    @Environment(EnvType.CLIENT)
    public static void handleEntryPointError(Throwable e) {
        crashReport = CrashReport.create(e, "Initializing game");
        crashReport.addElement("Initialization");
        MinecraftClient.addSystemDetailsToCrashReport(null, null, MinecraftVersion.create().getName(), null, crashReport);
        CrashUtils.outputClientReport(crashReport);

        // Make GL shuttup about any GL error that occurred
        Window.acceptError((integer, stringx) -> {
        });
    }


    @Environment(EnvType.CLIENT)
    public static void displayInitErrorScreen() {
        try {
            MinecraftClient.getInstance().setScreen(new InitErrorScreen(crashReport));
        } catch (Throwable t) {
            CrashReport additionalReport = CrashReport.create(t, "Displaying init error screen");
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            CrashUtils.outputClientReport(additionalReport);
            System.exit(additionalReport.getFile() != null ? -1 : -2);
        }
    }

}
