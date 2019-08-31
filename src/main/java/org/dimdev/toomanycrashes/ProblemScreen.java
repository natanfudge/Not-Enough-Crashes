package org.dimdev.toomanycrashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.HasteUpload;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public abstract class ProblemScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString = null;
    protected int xLeft = Integer.MAX_VALUE;
    protected int xRight = Integer.MIN_VALUE;
    protected int yTop = Integer.MAX_VALUE;
    protected int yBottom = Integer.MIN_VALUE;

    protected ProblemScreen(CrashReport report) {
        super(new LiteralText(""));
        this.report = report;
    }

    @Override
    public void init() {
        addButton(new ButtonWidget(width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20, I18n.translate("toomanycrashes.gui.getLink"),
                buttonWidget -> {
                    try {
                        if (hasteLink == null) {
                            hasteLink = HasteUpload.uploadToHaste(ModConfig.instance().hasteURL, "mccrash", report.asString());
                        }
                        Field uriField;
                        //noinspection JavaReflectionMemberAccess
                        uriField = Screen.class.getDeclaredField("clickedLink");
                        uriField.setAccessible(true);
                        uriField.set(ProblemScreen.this, new URI(hasteLink));
                        minecraft.openScreen(new ConfirmChatLinkScreen(b -> {
                            if (b) {
                                SystemUtil.getOperatingSystem().open(hasteLink);
                            }

                            this.minecraft.openScreen(this);
                        }, hasteLink, false));
                    } catch (Throwable e) {
                        LOGGER.error("Exception when crash menu button clicked:", e);
                        buttonWidget.setMessage(I18n.translate("toomanycrashes.gui.failed"));
                        buttonWidget.active = false;
                    }
                }));
    }

    @Override
    public boolean mouseClicked(double x, double y, int int_1) {
        if (x >= xLeft && x <= xRight && y >= yTop && y <= yBottom) {
            File file = report.getFile();
            if (file != null) {
                SystemUtil.getOperatingSystem().open(file);
            }
        }
        return super.mouseClicked(x, y, int_1);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected String getModListString() {
        if (modListString == null) {
            Set<ModMetadata> suspectedMods = ((PatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.translate("toomanycrashes.crashscreen.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModMetadata mod : suspectedMods) {
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

    protected void drawFileNameString(int y) {
        String fileNameString =
                report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.translate("toomanycrashes.crashscreen.reportSaveFailed");
        int stLen = font.getStringWidth(fileNameString);
        xLeft = width / 2 - stLen / 2;
        xRight = width / 2 + stLen / 2;
        drawString(font, fileNameString, xLeft, y += 11, 0x00FF00);
        yTop = y;
        yBottom = y + 10;
    }
}
