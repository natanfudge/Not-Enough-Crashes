package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.utils.NecLocalization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashReport;

@Environment(EnvType.CLIENT)
public class InitErrorScreen extends ProblemScreen {

    private static final int BODY_TEXT_COLOR = 0xD0D0D0;

    public InitErrorScreen(CrashReport report) {
        super(report);
    }

    /* ------------------------------------------------------------------ */
    /* Screen setup                                                       */
    /* ------------------------------------------------------------------ */
    @Override
    public void init() {
        super.init();

        /* ---------- “Quit” button ---------- */
        ButtonWidget exitButton = ButtonWidget.builder(
                        Text.translatable("menu.quit"),
                        btn -> System.exit(-1))
                .dimensions(width / 2 - 155, height / 4 + 120 + 12, 150, 20)
                .build();
        addDrawableChild(exitButton);

        /* ---------- Text widgets ---------- */
        int paragraphStartX = width / 2;
        int y = height / 4 - 40;   // same baseline offset used in CrashScreen

        // Title
        addDrawableChild(centeredText(paragraphStartX, y,
                Text.translatable("notenoughcrashes.initerrorscreen.title"), 0xFFFFFF));

        // Body – replicate original vertical gaps
        y = addBodyLine(paragraphStartX, y + 40,  0,  "notenoughcrashes.initerrorscreen.summary");
        y = addBodyLine(paragraphStartX, y,       18, "notenoughcrashes.crashscreen.paragraph1.line1");

        y = addBodyLine(paragraphStartX, y,       22, "notenoughcrashes.crashscreen.paragraph2.line1");
        y = addBodyLine(paragraphStartX, y,        9, "notenoughcrashes.crashscreen.paragraph2.line2");

        /* crash‑report file path */
        String fileName = getFileNameString();           // helper in ProblemScreen
        y = addBodyLine(paragraphStartX, y,       11, fileName, 0x00FF00);

        y = addBodyLine(paragraphStartX, y,       12, "notenoughcrashes.initerrorscreen.paragraph3.line1");
        y = addBodyLine(paragraphStartX, y,        9, "notenoughcrashes.initerrorscreen.paragraph3.line2");
        y = addBodyLine(paragraphStartX, y,        9, "notenoughcrashes.initerrorscreen.paragraph3.line3");
        addBodyLine(paragraphStartX, y,            9, "notenoughcrashes.initerrorscreen.paragraph3.line4");
    }

    /* ------------------------------------------------------------------ */
    /* Helpers (shared style with CrashScreen)                            */
    /* ------------------------------------------------------------------ */

    /** Returns a centred `TextWidget` positioned by *pixel* centre, not widget width. */
    private TextWidget centeredText(int centreX, int y, Text text, int color) {
        Text coloredText = text.copy().styled(style -> style.withColor(color));
        var w = new TextWidget(coloredText, textRenderer);
        w.setX(centreX - textRenderer.getWidth(text.getString()) / 2);
        w.setY(y);
        return w;
    }

    /** Overload that accepts a translation‑key or literal string. */
    private TextWidget centeredText(int centreX, int y, String keyOrLiteral, int color) {
        return centeredText(centreX, y, Text.translatable(keyOrLiteral), color);
    }

    /**
     * Adds a body line, preserving the spacing pattern used in CrashScreen.
     * Returns the Y coordinate of the line that was just placed.
     */
    private int addBodyLine(int centreX, int currentY, int offset, String keyOrLiteral) {
        return addBodyLine(centreX, currentY, offset, keyOrLiteral, BODY_TEXT_COLOR);
    }

    private int addBodyLine(int centreX, int currentY, int offset, String keyOrLiteral, int color) {
        int y = currentY + offset;
        addDrawableChild(centeredText(centreX, y, keyOrLiteral, color));
        return y;
    }

}
