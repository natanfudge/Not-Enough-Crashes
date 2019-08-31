package org.dimdev.toomanycrashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class InitErrorScreen extends ProblemScreen {

    public InitErrorScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void init() {
        super.init();
        AbstractButtonWidget getLinkButton = buttons.get(0);
        getLinkButton.x = width / 2 - 155;
        getLinkButton.y = height / 4 + 120 + 12;
        getLinkButton.setWidth(310);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        renderBackground();
        drawCenteredString(font, I18n.translate("toomanycrashes.initerrorscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(font, I18n.translate("toomanycrashes.initerrorscreen.summary"), x, y, textColor);
        drawString(font, I18n.translate("toomanycrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawCenteredString(font, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(font, I18n.translate("toomanycrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(font, I18n.translate("toomanycrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawFileNameString(y);
        y += 11;

        drawString(font, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(font, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(font, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(font, I18n.translate("toomanycrashes.initerrorscreen.paragraph3.line4"), x, y + 9, textColor);

        super.render(mouseX, mouseY, partialTicks);
    }
}
