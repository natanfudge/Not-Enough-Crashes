package fudge.notenoughcrashes.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MidnightConfigListWidget extends ContainerObjectSelectionList<ButtonEntry> {
    public boolean renderHeaderSeparator = true;

    public MidnightConfigListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @Override
    protected int scrollBarX() {
        return this.width - 7;
    }

    @Override
    protected void extractListSeparators(GuiGraphicsExtractor context) {
        if (renderHeaderSeparator)
            super.extractListSeparators(context);
        else
            context.blitSprite(RenderPipelines.GUI_TEXTURED, this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR, this.getX(), this.getBottom(), this.getWidth(), 2);
    }

    public void addButton(List<AbstractWidget> buttons, Component text, EntryInfo info) {
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
