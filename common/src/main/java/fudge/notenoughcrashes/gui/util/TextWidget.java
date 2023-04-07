package fudge.notenoughcrashes.gui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TextWidget implements Widget {
    private final Text text;
    private final String translated;

    private final int color;

    private final TextRenderer font;

    private final int x;
    private final int y;

    private final int width;

    final int startX;

    public static final int CLICKABLE_TEXT_COLOR = 0xE0E000;

    public TextWidget(Text text, int color, TextRenderer font, int x, int y) {
        this.text = text;
        this.color = color;
        this.font = font;
        this.x = x;
        this.y = y;
        translated = text.getString();
        width = MinecraftClient.getInstance().textRenderer.getWidth(translated);
        startX = x - width / 2;
    }

    @Override
    public void draw(MatrixStack stack) {
        DrawableHelper.drawCenteredTextWithShadow(stack,font, translated, x, y, color);
    }

    @Override
    public void onClick(double x, double y) {
        Text hoveredText = getTextAt(x, y);
        if (hoveredText != null) {
            Screen screen = MinecraftClient.getInstance().currentScreen;
            if (screen != null) {
                screen.handleTextClick(hoveredText.getStyle());
            }
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY) {
        final int endX = x + width / 2;
        int height = 8;
        final int startY = y - height / 2;
        final int endY = y + height / 2;

        return mouseX >= startX && mouseX <= endX && mouseY <= endY && mouseY >= startY;
    }


    private Text getTextAt(double x, double y) {
        if (isWithinBounds(x, y)) {
            int i = startX;

            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            for (Text component : getTextParts(text)) {
                i += renderer.getWidth(component.getString());
                if (i > x) {
                    return component;
                }
            }
        }
        return null;
    }

    private List<Text> getTextParts(Text text){
        List<Text> parts = new ArrayList<>();
        parts.add(text);
        parts.addAll(text.getSiblings());
        return parts;
    }



}