package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.ModConfig;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.crash.CrashReport;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class InitErrorScreen extends ProblemScreen {

    public InitErrorScreen(CrashReport report) {
        super(report);
    }

    private static final int TEXT_COLOR = 0xD0D0D0;

    @Override
    public void init() {
        super.init();
        AbstractButtonWidget getLinkButton = buttons.get(0);
        getLinkButton.x = width / 2 - 155;
        getLinkButton.y = height / 4 + 120 + 6;
        getLinkButton.setWidth(310);

        ButtonWidget exitButton = new ButtonWidget(width / 2 - 155, height / 4 + 120 + 12 + 18,
                        310, 20, I18n.translate("menu.quit"),
                        button -> System.exit(-1));

        addButton(exitButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        //TODO: "exit game" button
        renderBackground();
        drawCenteredString(font, I18n.translate("notenoughcrashes.initerrorscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        drawString(font, I18n.translate("notenoughcrashes.initerrorscreen.summary"), x, y, TEXT_COLOR);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph1.line1"), x, y + 18, TEXT_COLOR);


        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph2.line1"), x, y + 40, TEXT_COLOR);
        drawString(font, I18n.translate("notenoughcrashes.crashscreen.paragraph2.line2"), x, y + 49, TEXT_COLOR);

        drawFileNameString(y + 49);

        drawString(font, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line1"), x, y + 72, TEXT_COLOR);
        drawString(font, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line2"), x, y + 81, TEXT_COLOR);
        drawString(font, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line3"), x, y + 90, TEXT_COLOR);
        drawString(font, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line4"), x, y + 99, TEXT_COLOR);

        super.render(mouseX, mouseY, partialTicks);
    }
}
