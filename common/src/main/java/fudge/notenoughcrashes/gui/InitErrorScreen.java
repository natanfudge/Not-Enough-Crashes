package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
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

        ButtonWidget exitButton = ButtonWidget.builder(NecLocalization.translatedText("menu.quit"),
                button -> {
                    System.exit(-1);
                }).dimensions(width / 2 - 155, height / 4 + 120 + 12, 150, 20).build();

        addDrawableChild(exitButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseY, int i, float f) {
        renderBackground(matrixStack);
        drawCenteredTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.initerrorscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.initerrorscreen.summary"), x, y, TEXT_COLOR);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph1.line1"), x, y + 18, TEXT_COLOR);


        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line1"), x, y + 40, TEXT_COLOR);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line2"), x, y + 49, TEXT_COLOR);

        drawFileNameString(matrixStack, y + 49);

        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.initerrorscreen.paragraph3.line1"), x, y + 72, TEXT_COLOR);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.initerrorscreen.paragraph3.line2"), x, y + 81, TEXT_COLOR);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.initerrorscreen.paragraph3.line3"), x, y + 90, TEXT_COLOR);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.initerrorscreen.paragraph3.line4"), x, y + 99, TEXT_COLOR);

        super.render(matrixStack, mouseY, i, f);
    }

}
