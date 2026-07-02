package fudge.notenoughcrashes.config;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
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
    public TabNavigationBar tabNavigation;
    public Button done;
    public double scrollProgress = 0d;

    public MidnightConfigScreen(Screen parent, String modid) {
        super(Component.translatable(modid + ".midnightconfig.title"));
        this.parent = parent;
        this.modid = modid;
        this.translationPrefix = modid + ".midnightconfig.";
        this.instance = MidnightConfig.configInstances.get(modid);
        instance.loadValuesFromJson();
        MidnightConfig.entries.values().forEach(info -> {
            if (Objects.equals(info.modid, modid)) {
                String tabId = info.entry != null ? info.entry.category() : info.comment.category();
                String name = translationPrefix + "category." + tabId;
                if (!Language.getInstance().has(name) && tabId.equals("default"))
                    name = translationPrefix + "title";
                if (!tabs.containsKey(name)) {
                    info.tab = new GridLayoutTab(Component.translatable(name));
                    tabs.put(name, info.tab);
                } else info.tab = tabs.get(name);
            }
        });
        tabNavigation = createTabNavigation();
        tabNavigation.selectTab(0, false);
        tabNavigation.arrangeElements(this.width);
        prevTab = tabManager.getCurrentTab();
    }

    // Real Time config update //
    @Override
    public void tick() {
        super.tick();
        if (prevTab != null && prevTab != tabManager.getCurrentTab()) {
            prevTab = tabManager.getCurrentTab();
            updateList();
            list.setScrollAmount(0);
        }
        scrollProgress = list.scrollAmount();
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
                if (entry.buttons.get(0) instanceof AbstractWidget widget)
                    if (widget.isFocused() || widget.isHovered())
                        widget.setTooltip(entry.info.getTooltip(true));
                if (entry.buttons.get(1) instanceof Button button)
                    button.active = !Objects.equals(String.valueOf(entry.info.value), String.valueOf(entry.info.defaultValue)) && entry.info.conditionsMet;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return this.tabNavigation.keyPressed(input) || super.keyPressed(input);
    }

    @Override
    public void onClose() {
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
        Objects.requireNonNull(minecraft).setScreenAndShow(parent);
    }

    @Override
    public void init() {
        super.init();
        tabNavigation = createTabNavigation();
        tabNavigation.arrangeElements(this.width);
        if (tabs.size() > 1)
            this.addRenderableWidget(tabNavigation);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 - 154, this.height - 26, 150, 20).build());
        done = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            for (EntryInfo info : MidnightConfig.entries.values())
                if (info.modid.equals(modid))
                    info.updateFieldValue();
            MidnightConfig.write(modid);
            onClose();
        }).bounds(this.width / 2 + 4, this.height - 26, 150, 20).build());

        this.list = new MidnightConfigListWidget(this.minecraft, this.width, this.height - 57, 24, 25);
        this.addWidget(this.list);
        updateList();
        if (tabs.size() > 1)
            list.renderHeaderSeparator = false;
    }

    private TabNavigationBar createTabNavigation() {
        TabNavigationBar.Builder builder = TabNavigationBar.builder(tabManager, 0, 0, this.width, 24);
        tabs.values().forEach(tab -> builder.addTab(new TabButton(tabManager, tab, 0, 24) {
            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta) {
                context.centeredText(font, getMessage(), getX() + getWidth() / 2, getY() + 7, isSelected() ? 0xFFFFFFFF : 0xFFA0A0A0);
            }
        }, tab));
        return builder.build();
    }

    public void updateList() {
        this.list.clear();
        instance.onTabInit(prevTab.getTabTitle().getContents() instanceof TranslatableContents translatable ?
                translatable.getKey().substring(translatable.getKey().lastIndexOf('.') + 1) : prevTab.getTabTitle().toString(), list, this);
        for (EntryInfo info : MidnightConfig.entries.values()) {
            info.updateConditions();
            if (!info.conditionsMet) {
                boolean visibleButLocked = false;
                for (MidnightConfig.Condition condition : info.conditions)
                    visibleButLocked |= condition.visibleButLocked();
                if (!visibleButLocked) continue;
            }
            if (info.modid.equals(modid) && (info.tab == null || info.tab == tabManager.getCurrentTab())) {
                Button resetButton = Button.builder(Component.translatable("controls.reset"), (button -> {
                    info.value = info.defaultValue;
                    info.listIndex = 0;
                    info.tempValue = info.toTemporaryValue();
                    updateList();
                })).bounds(0, 0, 20, 20).build();
                resetButton.setPosition(width - 205 + 150 + 25, 0);

                if (info.function != null) {
                    AbstractWidget widget;
                    MidnightConfig.Entry e = info.entry;
                    if (info.function instanceof Map.Entry) { // Enums & booleans
                        var values = (Map.Entry<Button.OnPress, Function<Object, Component>>) info.function;
                        if (info.dataType.isEnum()) {
                            values.setValue(value -> instance.getEnumTranslatableText(value, info));
                        }
                        widget = Button.builder(values.getValue().apply(info.value), values.getKey()).bounds(width - 185, 0, 150, 20).tooltip(info.getTooltip(true)).build();
                    } else if (e.isSlider())
                        widget = new MidnightSliderWidget(width - 185, 0, 150, 20, Component.literal(info.tempValue), (Double.parseDouble(info.tempValue) - e.min()) / (e.max() - e.min()), info);
                    else
                        widget = new EditBox(font, width - 185, 0, 150, 20, Component.empty());

                    if (widget instanceof EditBox textField) {
                        textField.setMaxLength(e.width());
                        textField.setValue(info.tempValue);
                        Predicate<String> processor = ((BiFunction<EditBox, Button, Predicate<String>>) info.function).apply(textField, done);
                        textField.setResponder(processor::test);
                    }
                    widget.setTooltip(info.getTooltip(true));

                    Button cycleButton = null;
                    if (info.field.getType() == List.class) {
                        cycleButton = Button.builder(Component.literal(String.valueOf(info.listIndex)).withStyle(ChatFormatting.GOLD), (button -> {
                            var values = (List<?>) info.value;
                            values.remove("");
                            info.listIndex = info.listIndex != values.size() ? info.listIndex + 1 : 0;
                            info.tempValue = info.listIndex != values.size() ? info.toTemporaryValue() : "";
                            updateList();
                        })).bounds(width - 185, 0, 20, 20).tooltip(Tooltip.create(Component.translatable("midnightconfig.action.list_index", info.listIndex))).build();
                    }
                    if (e.isColor()) {
                        Button colorButton = Button.builder(Component.literal("⬛"),
                                button -> new Thread(() -> {
                                    Color newColor = JColorChooser.showDialog(null, Component.translatable("midnightconfig.colorChooser.title").getString(), Color.decode(!Objects.equals(info.tempValue, "") ? info.tempValue : "#FFFFFF"));
                                    if (newColor != null) {
                                        info.setValue("#" + Integer.toHexString(newColor.getRGB()).substring(2));
                                        updateList();
                                    }
                                }).start()
                        ).bounds(width - 185, 0, 20, 20).tooltip(Tooltip.create(Component.translatable("midnightconfig.action.color_chooser"))).build();
                        try {
                            colorButton.setMessage(Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                        } catch (Exception ignored) {
                        }
                        info.actionButton = colorButton;
                    } else if (e.selectionMode() > -1) {
                        Button explorerButton = Button.builder(Component.literal("..."),
                                button -> new Thread(() -> {
                                    JFileChooser fileChooser = new JFileChooser(info.tempValue);
                                    fileChooser.setFileSelectionMode(e.selectionMode());
                                    fileChooser.setDialogType(e.fileChooserType());
                                    fileChooser.setDialogTitle(Component.translatable(translationPrefix + info.fieldName + ".fileChooser").getString());
                                    if ((e.selectionMode() == JFileChooser.FILES_ONLY || e.selectionMode() == JFileChooser.FILES_AND_DIRECTORIES) && Arrays.stream(e.fileExtensions()).noneMatch("*"::equals))
                                        fileChooser.setFileFilter(new FileNameExtensionFilter(
                                                Component.translatable(translationPrefix + info.fieldName + ".fileFilter").getString(), e.fileExtensions()));
                                    if (fileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                                        info.setValue(fileChooser.getSelectedFile().getAbsolutePath());
                                        updateList();
                                    }
                                }).start()
                        ).bounds(0, 0, 20, 20).build();
                        explorerButton.setTooltip(Tooltip.create(Component.translatable("midnightconfig.action.file_chooser")));
                        explorerButton.setPosition(width - 185, 0);
                        info.actionButton = explorerButton;
                    }
                    List<AbstractWidget> widgets = Lists.newArrayList(widget, resetButton);

                    if (info.actionButton != null) {
                        if (Util.getPlatform() == Util.OS.OSX) info.actionButton.active = false;
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
                    this.list.addButton(widgets, Component.translatable(info.translationKey), info);
                } else this.list.addButton(List.of(), Component.translatable(info.translationKey), info);
            }
            list.setScrollAmount(scrollProgress);
            updateButtons();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.list.extractRenderState(context, mouseX, mouseY, delta);
        if (tabs.size() < 2) context.centeredText(font, title, width / 2, 10, 0xFFFFFFFF);
    }
}
