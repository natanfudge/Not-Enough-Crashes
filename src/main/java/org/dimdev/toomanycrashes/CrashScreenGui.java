package org.dimdev.toomanycrashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.MainMenuGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class CrashScreenGui extends ProblemScreenGui {
    public CrashScreenGui(CrashReport report) {
        super(report);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        ButtonWidget mainMenuButton = new ButtonWidget(0, width / 2 - 155, height / 4 + 120 + 12, 150, 20, I18n.translate("gui.toTitle")) {
            @Override
            public void onPressed(double x, double y) {
                client.openGui(new MainMenuGui());
            }
        };

        if (ModConfig.instance().disableReturnToMainMenu) {
            mainMenuButton.enabled = false;
            mainMenuButton.text = I18n.translate("toomanycrashes.gui.disabledByConfig");
        }

        addButton(mainMenuButton);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        drawBackground();
        drawStringCentered(fontRenderer, I18n.translate("toomanycrashes.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.summary"), x, y, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawStringCentered(fontRenderer, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        String fileNameString = report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.translate("toomanycrashes.crashscreen.reportSaveFailed");
        drawStringCentered(fontRenderer, fileNameString, width / 2, y += 11, 0x00FF00);

        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph3.line4"), x, y + 9, textColor);

        super.draw(mouseX, mouseY, partialTicks);
    }
}
