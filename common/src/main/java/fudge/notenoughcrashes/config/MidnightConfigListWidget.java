package fudge.notenoughcrashes.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

public class MidnightConfigListWidget extends ElementListWidget<ButtonEntry> {
    public boolean renderHeaderSeparator = true;

    public MidnightConfigListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @Override
    public int getScrollbarX() {
        return this.width - 7;
    }

    @Override
    public void drawHeaderAndFooterSeparators(DrawContext context) {
        if (renderHeaderSeparator)
            super.drawHeaderAndFooterSeparators(context);
        else
            context.drawTexture(RenderPipelines.GUI_TEXTURED, this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, this.getX(), this.getBottom(), 0, 0, this.getWidth(), 2, 32, 2);
    }

    public void addButton(List<ClickableWidget> buttons, Text text, EntryInfo info) {
        this.addEntry(new ButtonEntry(buttons, text, info));
    }

    public void clear() {
        this.clearEntries();
    }

    @Override
    public int getRowWidth() {
        return 10000;
    }
}