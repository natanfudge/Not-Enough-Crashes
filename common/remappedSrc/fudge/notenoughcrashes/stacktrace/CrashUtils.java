package fudge.notenoughcrashes.stacktrace;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashUtils {

    private static boolean isClient;

    static {
        try {
            isClient = MinecraftClient.getInstance() != null;
        } catch (NoClassDefFoundError e) {
            isClient = false;
        }
    }

    // We don't use the Mojang printCrashReport because it calls System.exit(), lol
    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += isClient && MinecraftClient.getInstance().isOnThread() ? "-client" : "-server";
                reportName += ".txt";

                File reportsDir = new File(NecPlatform.instance().getGameDirectory().toFile(), "crash-reports");
                File reportFile = new File(reportsDir, reportName);

                report.writeToFile(reportFile);
            }
        } catch (Throwable e) {
            NotEnoughCrashes.getLogger().fatal("Failed saving report", e);
        }

        NotEnoughCrashes.getLogger().fatal("Minecraft ran into a problem! " + (report.getFile() != null ? "Report saved to: " + report.getFile() :
                "Crash report could not be saved.") + "\n" +
                report.asString());
    }
}
