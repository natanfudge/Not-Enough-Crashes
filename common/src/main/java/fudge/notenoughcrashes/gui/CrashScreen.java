package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class CrashScreen extends ProblemScreen {

    @Override
    public ProblemScreen construct(CrashReport report) {
        return new CrashScreen(report);
    }

    public CrashScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void init() {
        super.init();
        ButtonWidget mainMenuButton = ButtonWidget.builder(
                        NecLocalization.translatedText("gui.toTitle"),
                        button -> MinecraftClient.getInstance().setScreen(new TitleScreen())
                )
                .dimensions(width / 2 - 155, height / 4 + 120 + 12, 150, 20)
                .build();

        if (NecConfig.getCurrent().disableReturnToMainMenu()) {
            mainMenuButton.active = false;
            mainMenuButton.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.disabledByConfig"));
        }

        addDrawableChild(mainMenuButton);
    }


    @Override
    public void render(MatrixStack matrixStack, int mouseY, int i, float f) {
        renderBackground(matrixStack);
        drawCenteredTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.summary"), x, y, textColor);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        y += 11;

        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawFileNameString(matrixStack, y);
        y += 11;

        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line3"), x, y += 9, textColor);
        drawTextWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line4"), x, y + 9, textColor);

        super.render(matrixStack, mouseY, i, f);
    }

}
