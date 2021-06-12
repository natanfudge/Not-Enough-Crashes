package fudge.notenoughcrashes.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class InitErrorScreen extends ProblemScreen {

    @Override
    public ProblemScreen construct(CrashReport report) {
        return new InitErrorScreen(report);
    }

    public InitErrorScreen(CrashReport report) {
        super(report);
    }

    private static final int TEXT_COLOR = 0xD0D0D0;

    @Override
    public void init() {
        super.init();
        getLinkButton.x = width / 2 - 155;
        getLinkButton.y = height / 4 + 120 + 6;
        getLinkButton.setWidth(310);

        ButtonWidget exitButton = new ButtonWidget(width / 2 - 155, height / 4 + 120 + 12 + 18,
                310, 20, new TranslatableText("menu.quit"),
                button -> System.exit(-1));

        addDrawableChild(exitButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseY, int i, float f) {
        renderBackground(matrixStack);
        drawCenteredText(matrixStack, textRenderer, I18n.translate("notenoughcrashes.initerrorscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.initerrorscreen.summary"), x, y, TEXT_COLOR);
        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.crashscreen.paragraph1.line1"), x, y + 18, TEXT_COLOR);


        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.crashscreen.paragraph2.line1"), x, y + 40, TEXT_COLOR);
        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.crashscreen.paragraph2.line2"), x, y + 49, TEXT_COLOR);

        drawFileNameString(matrixStack, y + 49);

        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line1"), x, y + 72, TEXT_COLOR);
        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line2"), x, y + 81, TEXT_COLOR);
        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line3"), x, y + 90, TEXT_COLOR);
        drawStringWithShadow(matrixStack, textRenderer, I18n.translate("notenoughcrashes.initerrorscreen.paragraph3.line4"), x, y + 99, TEXT_COLOR);

        super.render(matrixStack, mouseY, i, f);
    }

}
