package org.dimdev.toomanycrashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.ModInfo;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ingame.ConfirmChatLinkGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.HasteUpload;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public abstract class ProblemScreenGui extends Gui {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString = null;

    protected ProblemScreenGui(CrashReport report) {
        this.report = report;
    }

    @Override
    public void onInitialized() {
        addButton(new ButtonWidget(1, width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20, I18n.translate("toomanycrashes.gui.getLink", new Object[0])) {
            @Override
            public void onPressed(double x, double y) {
                try {
                    if (hasteLink == null) {
                        hasteLink = HasteUpload.uploadToHaste(ModConfig.instance().hasteURL, "mccrash", report.asString());
                    }
                    Field uriField;
                    try {
                        //noinspection JavaReflectionMemberAccess
                        uriField = Gui.class.getDeclaredField("field_2562");
                    } catch (NoSuchFieldException e) {
                        uriField = Gui.class.getDeclaredField("uri");
                    }
                    uriField.setAccessible(true);
                    uriField.set(ProblemScreenGui.this, new URI(hasteLink));
                    client.openGui(new ConfirmChatLinkGui(ProblemScreenGui.this, hasteLink, 31102009, false));
                } catch (Throwable e) {
                    LOGGER.error("Exception when crash menu button clicked:", e);
                    text = I18n.translate("toomanycrashes.gui.failed");
                    enabled = false;
                }
            }
        });
    }

    @Override
    public boolean doesEscapeKeyClose() {
        return false;
    }

    protected String getModListString() {
        if (modListString == null) {
            Set<ModInfo> suspectedMods = ((PatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.translate("toomanycrashes.crashscreen.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModInfo mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = I18n.translate("toomanycrashes.crashscreen.unknownCause");
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }
}
