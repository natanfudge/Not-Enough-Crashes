package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.summary"), x, y, textColor);
        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        y += 11;

        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawFileNameString(context, y);
        y += 11;

        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line3"), x, y += 9, textColor);
        context.drawTextWithShadow(textRenderer, NecLocalization.localize("notenoughcrashes.crashscreen.paragraph3.line4"), x, y + 9, textColor);

        super.render(context, mouseX, mouseY, delta);
    }

}
