package fudge.notenoughcrashes.config;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MidnightConfigScreen extends Screen {
    public MidnightConfig instance;
    public final String translationPrefix, modid;
    public final Screen parent;
    public MidnightConfigListWidget list;
    public TabManager tabManager = new TabManager(a -> {}, a -> {});
    public Map<String, Tab> tabs = new LinkedHashMap<>();
    public Tab prevTab;
    public TabNavigationWidget tabNavigation;
    public ButtonWidget done;
    public double scrollProgress = 0d;

    public MidnightConfigScreen(Screen parent, String modid) {
        super(Text.translatable(modid + ".midnightconfig.title"));
        this.parent = parent;
        this.modid = modid;
        this.translationPrefix = modid + ".midnightconfig.";
        this.instance = MidnightConfig.configInstances.get(modid);
        instance.loadValuesFromJson();
        MidnightConfig.entries.values().forEach(info -> {
            if (Objects.equals(info.modid, modid)) {
                String tabId = info.entry != null ? info.entry.category() : info.comment.category();
                String name = translationPrefix + "category." + tabId;
                if (!I18n.hasTranslation(name) && tabId.equals("default"))
                    name = translationPrefix + "title";
                if (!tabs.containsKey(name)) {
                    info.tab = new GridScreenTab(Text.translatable(name));
                    tabs.put(name, info.tab);
                } else info.tab = tabs.get(name);
            }
        });
        tabNavigation = TabNavigationWidget.builder(tabManager, this.width).tabs(tabs.values().toArray(new Tab[0])).build();
        tabNavigation.selectTab(0, false);
        tabNavigation.init();
        prevTab = tabManager.getCurrentTab();
    }

    // Real Time config update //
    @Override
    public void tick() {
        super.tick();
        if (prevTab != null && prevTab != tabManager.getCurrentTab()) {
            prevTab = tabManager.getCurrentTab();
            updateList();
            list.setScrollY(0);
        }
        scrollProgress = list.getScrollY();
        for (EntryInfo info : MidnightConfig.entries.values())
            if (Objects.equals(modid, info.modid)) info.updateFieldValue();
        updateButtons();
        if (instance.reloadScreen) {
            updateList();
            instance.reloadScreen = false;
        }
    }

    public void updateButtons() {
        if (this.list == null) return;

        for (ButtonEntry entry : this.list.children()) {
            if (entry.buttons != null && entry.buttons.size() > 1 && entry.info.field != null) {
                if (entry.buttons.get(0) instanceof ClickableWidget widget)
                    if (widget.isFocused() || widget.isHovered())
                        widget.setTooltip(entry.info.getTooltip(true));
                if (entry.buttons.get(1) instanceof ButtonWidget button)
                    button.active = !Objects.equals(String.valueOf(entry.info.value), String.valueOf(entry.info.defaultValue)) && entry.info.conditionsMet;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return this.tabNavigation.keyPressed(input) || super.keyPressed(input);
    }

    @Override
    public void close() {
        instance.loadValuesFromJson();
        MidnightConfig.entries.values().forEach(info -> {
            info.error = null;
            info.value = null;
            info.tempValue = null;
            info.actionButton = null;
            info.listIndex = 0;
            info.tab = null;
            info.inLimits = true;
        });
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void init() {
        super.init();
        tabNavigation.setWidth(this.width);
        tabNavigation.init();
        if (tabs.size() > 1)
            this.addDrawableChild(tabNavigation);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(this.width / 2 - 154, this.height - 26, 150, 20).build());
        done = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            for (EntryInfo info : MidnightConfig.entries.values())
                if (info.modid.equals(modid))
                    info.updateFieldValue();
            MidnightConfig.write(modid);
            close();
        }).dimensions(this.width / 2 + 4, this.height - 26, 150, 20).build());

        this.list = new MidnightConfigListWidget(this.client, this.width, this.height - 57, 24, 25);
        this.addSelectableChild(this.list);
        updateList();
        if (tabs.size() > 1)
            list.renderHeaderSeparator = false;
    }

    public void updateList() {
        this.list.clear();
        instance.onTabInit(prevTab.getTitle().getContent() instanceof TranslatableTextContent translatable ?
                translatable.getKey().substring(translatable.getKey().lastIndexOf('.') + 1) : prevTab.getTitle().toString(), list, this);
        for (EntryInfo info : MidnightConfig.entries.values()) {
            info.updateConditions();
            if (!info.conditionsMet) {
                boolean visibleButLocked = false;
                for (MidnightConfig.Condition condition : info.conditions)
                    visibleButLocked |= condition.visibleButLocked();
                if (!visibleButLocked) continue;
            }
            if (info.modid.equals(modid) && (info.tab == null || info.tab == tabManager.getCurrentTab())) {
                TextIconButtonWidget resetButton = TextIconButtonWidget.builder(Text.translatable("controls.reset"), (button -> {
                    info.value = info.defaultValue;
                    info.listIndex = 0;
                    info.tempValue = info.toTemporaryValue();
                    updateList();
                }), true).texture(Identifier.of("midnightlib", "icon/reset"), 12, 12).dimension(20, 20).build();
                resetButton.setPosition(width - 205 + 150 + 25, 0);

                if (info.function != null) {
                    ClickableWidget widget;
                    MidnightConfig.Entry e = info.entry;
                    if (info.function instanceof Map.Entry) { // Enums & booleans
                        var values = (Map.Entry<ButtonWidget.PressAction, Function<Object, Text>>) info.function;
                        if (info.dataType.isEnum()) {
                            values.setValue(value -> instance.getEnumTranslatableText(value, info));
                        }
                        widget = ButtonWidget.builder(values.getValue().apply(info.value), values.getKey()).dimensions(width - 185, 0, 150, 20).tooltip(info.getTooltip(true)).build();
                    } else if (e.isSlider())
                        widget = new MidnightSliderWidget(width - 185, 0, 150, 20, Text.of(info.tempValue), (Double.parseDouble(info.tempValue) - e.min()) / (e.max() - e.min()), info);
                    else
                        widget = new TextFieldWidget(textRenderer, width - 185, 0, 150, 20, Text.empty());

                    if (widget instanceof TextFieldWidget textField) {
                        textField.setMaxLength(e.width());
                        textField.setText(info.tempValue);
                        Predicate<String> processor = ((BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) info.function).apply(textField, done);
                        textField.setTextPredicate(processor);
                    }
                    widget.setTooltip(info.getTooltip(true));

                    ButtonWidget cycleButton = null;
                    if (info.field.getType() == List.class) {
                        cycleButton = ButtonWidget.builder(Text.literal(String.valueOf(info.listIndex)).formatted(Formatting.GOLD), (button -> {
                            var values = (List<?>) info.value;
                            values.remove("");
                            info.listIndex = info.listIndex != values.size() ? info.listIndex + 1 : 0;
                            info.tempValue = info.listIndex != values.size() ? info.toTemporaryValue() : "";
                            updateList();
                        })).dimensions(width - 185, 0, 20, 20).tooltip(Tooltip.of(Text.translatable("midnightconfig.action.list_index", info.listIndex))).build();
                    }
                    if (e.isColor()) {
                        ButtonWidget colorButton = ButtonWidget.builder(Text.literal("⬛"),
                                button -> new Thread(() -> {
                                    Color newColor = JColorChooser.showDialog(null, Text.translatable("midnightconfig.colorChooser.title").getString(), Color.decode(!Objects.equals(info.tempValue, "") ? info.tempValue : "#FFFFFF"));
                                    if (newColor != null) {
                                        info.setValue("#" + Integer.toHexString(newColor.getRGB()).substring(2));
                                        updateList();
                                    }
                                }).start()
                        ).dimensions(width - 185, 0, 20, 20).tooltip(Tooltip.of(Text.translatable("midnightconfig.action.color_chooser"))).build();
                        try {
                            colorButton.setMessage(Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                        } catch (Exception ignored) {
                        }
                        info.actionButton = colorButton;
                    } else if (e.selectionMode() > -1) {
                        ButtonWidget explorerButton = TextIconButtonWidget.builder(Text.empty(),
                                button -> new Thread(() -> {
                                    JFileChooser fileChooser = new JFileChooser(info.tempValue);
                                    fileChooser.setFileSelectionMode(e.selectionMode());
                                    fileChooser.setDialogType(e.fileChooserType());
                                    fileChooser.setDialogTitle(Text.translatable(translationPrefix + info.fieldName + ".fileChooser").getString());
                                    if ((e.selectionMode() == JFileChooser.FILES_ONLY || e.selectionMode() == JFileChooser.FILES_AND_DIRECTORIES) && Arrays.stream(e.fileExtensions()).noneMatch("*"::equals))
                                        fileChooser.setFileFilter(new FileNameExtensionFilter(
                                                Text.translatable(translationPrefix + info.fieldName + ".fileFilter").getString(), e.fileExtensions()));
                                    if (fileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                                        info.setValue(fileChooser.getSelectedFile().getAbsolutePath());
                                        updateList();
                                    }
                                }).start(), true
                        ).texture(Identifier.of("midnightlib", "icon/explorer"), 12, 12).dimension(20, 20).build();
                        explorerButton.setTooltip(Tooltip.of(Text.translatable("midnightconfig.action.file_chooser")));
                        explorerButton.setPosition(width - 185, 0);
                        info.actionButton = explorerButton;
                    }
                    List<ClickableWidget> widgets = Lists.newArrayList(widget, resetButton);

                    if (info.actionButton != null) {
                        if (Util.getOperatingSystem() == Util.OperatingSystem.OSX) info.actionButton.active = false;
                        widget.setWidth(widget.getWidth() - 22);
                        widget.setX(widget.getX() + 22);
                        widgets.add(info.actionButton);
                    }
                    if (cycleButton != null) {
                        if (info.actionButton != null) info.actionButton.setX(info.actionButton.getX() + 22);
                        widget.setWidth(widget.getWidth() - 22);
                        widget.setX(widget.getX() + 22);
                        widgets.add(cycleButton);
                    }
                    if (!info.conditionsMet) widgets.forEach(w -> w.active = false);
                    this.list.addButton(widgets, Text.translatable(info.translationKey), info);
                } else this.list.addButton(List.of(), Text.translatable(info.translationKey), info);
            }
            list.setScrollY(scrollProgress);
            updateButtons();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.list.render(context, mouseX, mouseY, delta);
        if (tabs.size() < 2) context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFFFF);
    }
}