package org.dimdev.toomanycrashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class InitErrorScreenGui extends ProblemScreenGui {

    public InitErrorScreenGui(CrashReport report) {
        super(report);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        ButtonWidget getLinkButton = buttons.get(0);
        getLinkButton.x = width / 2 - 155;
        getLinkButton.y = height / 4 + 120 + 12;
        getLinkButton.setWidth(310);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        drawBackground();
        drawStringCentered(fontRenderer, I18n.translate("toomanycrashes.initerrorscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(fontRenderer, I18n.translate("toomanycrashes.initerrorscreen.summary"), x, y, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawStringCentered(fontRenderer, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawStringCentered(fontRenderer, report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.translate("toomanycrashes.crashscreen.reportSaveFailed"), width / 2, y += 11, 0x00FF00);

        drawString(fontRenderer, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line4"), x, y + 9, textColor);

        super.draw(mouseX, mouseY, partialTicks);
    }
}
