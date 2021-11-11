package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.NecConfig;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
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
        ButtonWidget mainMenuButton = new ButtonWidget(width / 2 - 155, height / 4 + 120 + 12, 150, 20, new TranslatableText("gui.toTitle"),
                button -> MinecraftClient.getInstance().openScreen(new TitleScreen()));

        if (NecConfig.instance().disableReturnToMainMenu) {
            mainMenuButton.active = false;
            mainMenuButton.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.disabledByConfig"));
        }

        addButton(mainMenuButton);
    }


    @Override
    public void render(MatrixStack matrixStack, int mouseY, int i, float f) {
        renderBackground(matrixStack);
        drawCenteredText(matrixStack, textRenderer, NecLocalization.translatedText("notenoughcrashes.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.summary"), x, y, textColor);
        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        y += 11;

        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawFileNameString(matrixStack, y);
        y += 11;

        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line3"), x, y += 9, textColor);
        drawStringWithShadow(matrixStack, textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line4"), x, y + 9, textColor);

        super.render(matrixStack, mouseY, i, f);
    }

}
