package fudge.notenoughcrashes.config;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ButtonEntry extends ElementListWidget.Entry<ButtonEntry> {
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    public final Text text;
    public final List<ClickableWidget> buttons;
    public final EntryInfo info;
    public boolean centered = false;
    public MultilineTextWidget title;

    public ButtonEntry(List<ClickableWidget> buttons, Text text, EntryInfo info) {
        this.buttons = buttons;
        this.text = text;
        this.info = info;
        if (info != null && info.comment != null)
            this.centered = info.comment.centered();
        int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();

        if (text != null && (!text.getString().contains("spacer") || !buttons.isEmpty())) {
            title = new MultilineTextWidget(12, 0, Text.of(text), textRenderer).setCentered(centered);
            if (info != null)
                title.setTooltip(info.getTooltip(false));
            title.setMaxWidth(!buttons.isEmpty() ? buttons.get(buttons.size() > 2 ? buttons.size() - 1 : 0).getX() - 16 : scaledWidth - 24);
            if (centered) title.setX(scaledWidth / 2 - (title.getWidth() / 2));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        buttons.forEach(b -> {
            b.setY(this.getY());
            b.render(context, mouseX, mouseY, tickDelta);
        });
        if (title != null) {
            title.setY(this.getY() + 5);
            title.render(context, mouseX, mouseY, tickDelta);

            if (info.entry != null && !this.buttons.isEmpty() && this.buttons.getFirst() instanceof ClickableWidget widget) {
                int idMode = this.info.entry.idMode();
                if (idMode != -1) context.drawItem(idMode == 0 ?
                                Registries.ITEM.get(Identifier.tryParse(this.info.tempValue)).getDefaultStack()
                                : Registries.BLOCK.get(Identifier.tryParse(this.info.tempValue)).asItem().getDefaultStack(),
                        widget.getX() + widget.getWidth() - 18, this.getY() + 2);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.info != null && this.info.comment != null && !this.info.comment.url().isBlank())
            ConfirmLinkScreen.open(MinecraftClient.getInstance().currentScreen, this.info.comment.url(), true);
        return super.mouseClicked(click, doubled);
    }

    public List<? extends Element> children() {
        return Lists.newArrayList(buttons);
    }

    public List<? extends Selectable> selectableChildren() {
        return Lists.newArrayList(buttons);
    }
}