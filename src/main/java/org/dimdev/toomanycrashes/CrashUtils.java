package org.dimdev.toomanycrashes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashUtils {
    private static final Logger LOGGER = LogManager.getLogger("TMC");
    private static boolean isClient;

    static {
        try {
            isClient = MinecraftClient.getInstance() != null;
        } catch (NoClassDefFoundError e) {
            isClient = false;
        }
    }

    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += MinecraftClient.getInstance().isMainThread() ? "-client" : "-server";
                reportName += ".txt";

                File reportsDir = isClient ? new File(MinecraftClient.getInstance().runDirectory, "crash-reports") : new File("crash-reports");
                File reportFile = new File(reportsDir, reportName);

                report.writeToFile(reportFile);
            }
        } catch (Throwable e) {
            LOGGER.fatal("Failed saving report", e);
        }

        LOGGER.fatal("Minecraft ran into a problem! " + (report.getFile() != null ? "Report saved to: " + report.getFile() : "Crash report could not be saved.") + "\n" +
                     report.asString());
    }
}
