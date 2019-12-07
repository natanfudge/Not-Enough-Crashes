package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.mixins.client.SplashScreenMixin;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;

import net.fabricmc.loader.api.FabricLoader;

public class EntryPointCatcher {
    private static Throwable entryPointCrash = null;

    public static boolean crashedDuringStartup() {
        return entryPointCrash != null;
    }

    private static final Logger LOGGER = LogManager.getLogger(NotEnoughCrashes.NAME + " Entry Points");

    private static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    public static void handleEntryPointError(Throwable e) {
        entryPointCrash = e;
        // Make it obvious the game crashed
        SplashScreenMixin.setLogo(new Identifier(NotEnoughCrashes.MOD_ID,"textures/game_crashed.png"));
    }

    public static void displayInitErrorScreen() {
        CrashReport crashReport = CrashReport.create(entryPointCrash, "Initializing game");
        crashReport.addElement("Initialization");
        MinecraftClient.addSystemDetailsToCrashReport(null, new MinecraftVersion().getName(), null, crashReport);
        CrashUtils.outputReport(crashReport);

        // Displaying a screen is just a nuisance in dev
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        try {
            getClient().openScreen(new InitErrorScreen(crashReport));
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            CrashUtils.outputReport(crashReport);
            System.exit(crashReport.getFile() != null ? -1 : -2);
        }
    }
}
