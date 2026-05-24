package fudge.notenoughcrashes.config;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ButtonEntry extends ContainerObjectSelectionList.Entry<ButtonEntry> {
    private static final Font textRenderer = Minecraft.getInstance().font;
    public final Component text;
    public final List<AbstractWidget> buttons;
    public final EntryInfo info;
    public boolean centered = false;
    public MultiLineTextWidget title;

    public ButtonEntry(List<AbstractWidget> buttons, Component text, EntryInfo info) {
        this.buttons = buttons;
        this.text = text;
        this.info = info;
        if (info != null && info.comment != null)
            this.centered = info.comment.centered();
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();

        if (text != null && (!text.getString().contains("spacer") || !buttons.isEmpty())) {
            title = new MultiLineTextWidget(12, 0, text, textRenderer).setCentered(centered);
            if (info != null)
                title.setTooltip(info.getTooltip(false));
            title.setMaxWidth(!buttons.isEmpty() ? buttons.get(buttons.size() > 2 ? buttons.size() - 1 : 0).getX() - 16 : scaledWidth - 24);
            if (centered) title.setX(scaledWidth / 2 - (title.getWidth() / 2));
        }
    }

    public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        buttons.forEach(b -> {
            b.setY(this.getY());
            b.extractRenderState(context, mouseX, mouseY, tickDelta);
        });
        if (title != null) {
            title.setY(this.getY() + 5);
            title.extractRenderState(context, mouseX, mouseY, tickDelta);

            if (info.entry != null && !this.buttons.isEmpty() && this.buttons.getFirst() instanceof AbstractWidget widget) {
                int idMode = this.info.entry.idMode();
                if (idMode != -1) context.item(idMode == 0 ?
                                BuiltInRegistries.ITEM.getValue(Identifier.tryParse(this.info.tempValue)).getDefaultInstance()
                                : BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(this.info.tempValue)).asItem().getDefaultInstance(),
                        widget.getX() + widget.getWidth() - 18, this.getY() + 2);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.info != null && this.info.comment != null && !this.info.comment.url().isBlank())
            ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().screen, this.info.comment.url(), true);
        return super.mouseClicked(click, doubled);
    }

    public List<? extends GuiEventListener> children() {
        return Lists.newArrayList(buttons);
    }

    public List<? extends NarratableEntry> narratables() {
        return Lists.newArrayList(buttons);
    }
}
