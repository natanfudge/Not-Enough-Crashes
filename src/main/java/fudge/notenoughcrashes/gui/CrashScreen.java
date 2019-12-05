package fudge.notenoughcrashes;

import fudge.notenoughcrashes.gui.util.TextWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class CrashScreen extends ProblemScreen {

    public CrashScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void init() {
        super.init();
        ButtonWidget mainMenuButton = new ButtonWidget(width / 2 - 155, height / 4 + 120 + 12, 150, 20, I18n.translate("gui.toTitle"),
                button -> minecraft.openScreen(new TitleScreen()));

        if (ModConfig.instance().disableReturnToMainMenu) {
            mainMenuButton.active = false;
            mainMenuButton.setMessage(I18n.translate("notenoughcrashes.gui.disabledByConfig"));
        }

        addButton(mainMenuButton);

        addWidget(new TextWidget(new LiteralText()));
    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        renderBackground();
        drawCenteredString(font, I18n.translate("notenoughcrashes.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(font, I18n.translate("notenoughcrashes.crashscreen.summary"), x, y, textColor);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawCenteredString(font, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawFileNameString(y);
        y += 11;

        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph3.line4"), x, y + 9, textColor);

        super.render(mouseX, mouseY, partialTicks);
    }
}
