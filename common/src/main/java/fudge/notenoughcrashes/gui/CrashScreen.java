package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.CrashReport;

public class CrashScreen extends ProblemScreen {

    private static final int BODY_TEXT_COLOR = 0xD0D0D0;

    public CrashScreen(CrashReport report) {
        super(report);
    }


    @Override
    public void init() {
        super.init();

        /* ---------- “Return to title” button ---------- */
        Button mainMenuButton = Button.builder(
                        NecLocalization.translatedText("gui.toTitle"),
                        btn -> {
                            InGameCatcher.crashScreenActive = true;
                            Minecraft.getInstance().setScreenAndShow(new TitleScreen());
                        })
                .bounds(width / 2 - 155, height / 4 + 120 + 12, 150, 20)
                .build();

        if (NecConfig.getCurrent().disableReturnToMainMenu()) {
            mainMenuButton.active = false;
            mainMenuButton.setMessage(
                    NecLocalization.translatedText("notenoughcrashes.gui.disabledByConfig"));
        }
        addRenderableWidget(mainMenuButton);

        /* ---------- Component widgets ---------- */
        int paragraphStartX = width / 2;
        int y = height / 4 - 51;        // title is 40 px above the old body start

        // Title
        addRenderableWidget(centeredText(paragraphStartX, y,
                Component.translatable("notenoughcrashes.crashscreen.title"), 0xFFFFFF));

        // Body copy – keeps the original spacing
        y = addBodyLine(paragraphStartX, y + 40, 18, "notenoughcrashes.crashscreen.summary");
        y = addBodyLine(paragraphStartX, y,       11, "notenoughcrashes.crashscreen.paragraph1.line1");

        y += 11; // blank row between paragraphs

        y = addBodyLine(paragraphStartX, y,       11, "notenoughcrashes.crashscreen.paragraph2.line1");
        y = addBodyLine(paragraphStartX, y,        9, "notenoughcrashes.crashscreen.paragraph2.line2");

        /* crash‑report file path */
        String fileName = getFileNameString();   // helper in ProblemScreen
        y = addBodyLine(paragraphStartX, y,       11, fileName, 0x00FF00);

        y = addBodyLine(paragraphStartX, y,       12, "notenoughcrashes.crashscreen.paragraph3.line1");
        y = addBodyLine(paragraphStartX, y,        9, "notenoughcrashes.crashscreen.paragraph3.line2");
        y = addBodyLine(paragraphStartX, y,        9, "notenoughcrashes.crashscreen.paragraph3.line3");
        addBodyLine(paragraphStartX, y,            9, "notenoughcrashes.crashscreen.paragraph3.line4");
    }

    /* --------------------------------------------------------------------- */
    /* Helpers                                                               */
    /* --------------------------------------------------------------------- */

    /** Convenience factory for a centred `StringWidget`. */
    private StringWidget centeredText(int centreX, int y, Component text, int color) {
        Component coloredText = text.copy().withColor(color);
        var w = new StringWidget(coloredText, font);
        w.setX(centreX - font.width(text.getString()) / 2);
        w.setY(y);
        return w;
    }

    /** Overload for translation keys. */
    private StringWidget centeredText(int centreX, int y, String translationKey, int color) {
        return centeredText(centreX, y, Component.translatable(translationKey), color);
    }

    /**
     * Add a body line, returning the new Y coordinate so that callers can keep
     * the original spacing logic.
     */
    private int addBodyLine(int centreX, int currentY, int offset, String keyOrLiteral) {
        return addBodyLine(centreX, currentY, offset, keyOrLiteral, BODY_TEXT_COLOR);
    }

    /**
     * Add a body line, returning the new Y coordinate so that callers can keep
     * the original spacing logic.
     */
    private int addBodyLine(int centreX, int currentY, int offset, String keyOrLiteral, int color) {
        int y = currentY + offset;
        addRenderableWidget(centeredText(centreX, y, keyOrLiteral, color));
        return y;
    }


}
