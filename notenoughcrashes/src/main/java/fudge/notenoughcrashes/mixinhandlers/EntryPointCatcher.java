package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.mixins.client.SplashScreenMixin;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
        MinecraftClient.addSystemDetailsToCrashReport(null,  MinecraftVersion.create().getName(), null, crashReport);
        CrashUtils.outputReport(crashReport);

        // Make GL shuttup about any GL error that occurred
        Window.acceptError((integer, stringx) -> {
        });

        // Make it obvious the game crashed
        SplashScreenMixin.setLogo(new Identifier(NotEnoughCrashes.MOD_ID, "textures/game_crashed.png"));
    }
    @Environment(EnvType.CLIENT)
    public static void displayInitErrorScreen() {
        try {
            MinecraftClient.getInstance().openScreen(new InitErrorScreen(crashReport));
        } catch (Throwable t) {
            CrashReport additionalReport = CrashReport.create(t, "Displaying init error screen");
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            CrashUtils.outputReport(additionalReport);
            System.exit(additionalReport.getFile() != null ? -1 : -2);
        }
    }

}
