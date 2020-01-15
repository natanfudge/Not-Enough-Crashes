package fudge.notenoughcrashes.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TextWidget implements Widget {
    private final Text text;
    private final String translated;

    private final int color;

    private final TextRenderer font;

    private final int x;
    private final int y;

    private final int width;
    private final int height = 8;

    final int startX;

    public static final int DEFAULT_TEXT_COLOR = 0x404040;
    public static final int CLICKABLE_TEXT_COLOR = 0xE0E000;


    private Screen getScreen() {
        return MinecraftClient.getInstance().currentScreen;
    }

    public TextWidget(Text text, int color, TextRenderer font, int x, int y) {
        this.text = text;
        this.color = color;
        this.font = font;
        this.x = x;
        this.y = y;
        translated = text.asFormattedString();
        width = MinecraftClient.getInstance().textRenderer.getStringWidth(translated);
        startX = x - width / 2;
    }

    @Override
    public void draw() {
        getScreen().drawCenteredString(font, translated, x, y, color);
    }

    @Override
    public void onClick(double x, double y) {
        Text hoveredText = getTextAt(x, y);
        if (hoveredText != null) {
            Screen screen = MinecraftClient.getInstance().currentScreen;
            if (screen != null) {
                screen.handleComponentClicked(hoveredText);
            }
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY) {
        final int endX = x + width / 2;
        final int startY = y - height / 2;
        final int endY = y + height / 2;

        return mouseX >= startX && mouseX <= endX && mouseY <= endY && mouseY >= startY;
    }


    private Text getTextAt(double x, double y) {
        if (isWithinBounds(x, y)) {
            int i = startX;
            for (Text component : text) {
                TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
                i += renderer.getStringWidth(component.asFormattedString());
                if (i > x) {
                    return component;
                }
            }
        }
        return null;
    }

}