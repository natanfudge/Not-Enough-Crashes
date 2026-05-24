package fudge.notenoughcrashes.stacktrace;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashUtils {

//    private static boolean isClient;
//
//    static {
//        try {
//            isClient = Minecraft.getInstance() != null;
//        } catch (NoClassDefFoundError e) {
//            isClient = false;
//        }
//    }

    public static void outputClientReport(CrashReport report) {
        outputReport(report,true);
    }

    // We don't use the Mojang printCrashReport because it calls System.exit(), lol
    public static void outputReport(CrashReport report, boolean isClient) {
        try {
            if (report.getSaveFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += isClient && Minecraft.getInstance().isSameThread() ? "-client" : "-server";
                reportName += ".txt";

                Path reportsDir = NecPlatform.instance().getGameDirectory().resolve("crash-reports");
                Path reportFile = reportsDir.resolve(reportName);

                report.saveToFile(reportFile, ReportType.CRASH);
            }
        } catch (Throwable e) {
            NotEnoughCrashes.getLogger().fatal("Failed saving report", e);
        }

        NotEnoughCrashes.getLogger().fatal("Minecraft ran into a problem! " + (report.getSaveFile() != null ? "Report saved to: " + report.getSaveFile() :
                "Crash report could not be saved.") + "\n" +
                report.getFriendlyReport(ReportType.CRASH));
    }
}
