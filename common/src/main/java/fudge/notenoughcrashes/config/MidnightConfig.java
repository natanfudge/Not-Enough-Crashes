package fudge.notenoughcrashes.config;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.api.EnvType; import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient; import net.minecraft.client.font.TextRenderer; import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element; import net.minecraft.client.gui.Selectable; import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab; import net.minecraft.client.gui.tab.Tab; import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip; import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style; import net.minecraft.text.Text;
import net.minecraft.util.Formatting; import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.swing.*; import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.io.IOException;
import java.lang.annotation.ElementType; import java.lang.annotation.Retention; import java.lang.annotation.RetentionPolicy; import java.lang.annotation.Target;
import java.lang.reflect.Field; import java.lang.reflect.Modifier; import java.lang.reflect.ParameterizedType;
import java.nio.file.Files; import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction; import java.util.function.Function; import java.util.function.Predicate;
import java.util.regex.Pattern;

import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

/** MidnightConfig v2.6.0 by Martin "Motschen" Prokoph
 *  Single class config library - feel free to copy!
 *  Based on <a href="https://github.com/Minenash/TinyConfig">...</a>
 *  Credits to Minenash */

@SuppressWarnings("unchecked")
public abstract class MidnightConfig {
    private static final Pattern INTEGER_ONLY = Pattern.compile("(-?[0-9]*)");
    private static final Pattern DECIMAL_ONLY = Pattern.compile("-?(\\d+\\.?\\d*|\\d*\\.?\\d+|\\.)");
    private static final Pattern HEXADECIMAL_ONLY = Pattern.compile("(-?[#0-9a-fA-F]*)");

    private static final List<EntryInfo> entries = new ArrayList<>();

    public static class EntryInfo {
        Field field;
        Class<?> dataType;
        int width, listIndex;
        boolean centered;
        Object defaultValue, value, function;
        String modid, tempValue;   // The value visible in the config screen
        boolean inLimits = true;
        Text name, error;
        ClickableWidget actionButton; // color picker button / explorer button
        Tab tab;

        public void setValue(Object value) {
            if (this.field.getType() != List.class) { this.value = value;
                this.tempValue = value.toString();
            } else { writeList(this.listIndex, value);
                this.tempValue = toTemporaryValue(); }
        }
        public String toTemporaryValue() {
            if (this.field.getType() != List.class) return this.value.toString();
            else try { return ((List<?>) this.value).get(this.listIndex).toString(); } catch (Exception ignored) {return "";}
        }
        public <T> void writeList(int index, T value) {
            var list = (List<T>) this.value;
            if (index >= list.size()) list.add(value);
            else list.set(index, value);
        }
    }

    public static final Map<String, Class<? extends MidnightConfig>> configClass = new HashMap<>();
    private static Path path;

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).excludeFieldsWithModifiers(Modifier.PRIVATE)
            .addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy())
            .registerTypeAdapter(Identifier.class, new TypeAdapter<Identifier>() {
                public void write(JsonWriter out, Identifier id) throws IOException { out.value(id.toString()); }
                public Identifier read(JsonReader in) throws IOException { return Identifier.of(in.nextString()); }
            })
            .setPrettyPrinting().create();

    @SuppressWarnings("unused") // Utility for mod authors
    public static @Nullable Object getDefaultValue(String modid, String entry) {
        for (EntryInfo e : entries) {
            if (modid.equals(e.modid) && entry.equals(e.field.getName())) return e.defaultValue;
        } return null;
    }

    public static void init(String modid, Class<? extends MidnightConfig> config) {
        path = NecPlatform.instance().getConfigDirectory().resolve(modid + ".json");
        configClass.put(modid, config);

        for (Field field : config.getFields()) {
            EntryInfo info = new EntryInfo();
            if ((field.isAnnotationPresent(Entry.class) || field.isAnnotationPresent(Comment.class)) && !field.isAnnotationPresent(Server.class) && !field.isAnnotationPresent(Hidden.class)
                    && NecPlatform.instance().isClient())
                initClient(modid, field, info);
            if (field.isAnnotationPresent(Entry.class))
                try { info.defaultValue = field.get(null);
                } catch (IllegalAccessException ignored) {}
        }
        try { gson.fromJson(Files.newBufferedReader(path), config); }
        catch (Exception e) { write(modid); }

        for (EntryInfo info : entries) {
            if (info.field.isAnnotationPresent(Entry.class)) try {
                info.value = info.field.get(null);
                info.tempValue = info.toTemporaryValue();
            } catch (IllegalAccessException ignored) {}
        }
    }
    @SuppressWarnings("ConstantValue") //pertains to requiredModLoaded
    @Environment(EnvType.CLIENT)
    private static void initClient(String modid, Field field, EntryInfo info) {
        info.dataType = getUnderlyingType(field);
        Entry e = field.getAnnotation(Entry.class);
        Comment c = field.getAnnotation(Comment.class);
        info.width = e != null ? e.width() : 0;
        info.field = field; info.modid = modid;
        boolean requiredModLoaded = true;

        if (e != null) {
            if (!e.requiredMod().isEmpty()) requiredModLoaded = NecPlatform.instance().isModLoaded(e.requiredMod());

            if (!requiredModLoaded) return;
            if (!e.name().isEmpty()) info.name = Text.translatable(e.name());
            if (info.dataType == int.class) textField(info, Integer::parseInt, INTEGER_ONLY, (int) e.min(), (int) e.max(), true);
            else if (info.dataType == float.class) textField(info, Float::parseFloat, DECIMAL_ONLY, (float) e.min(), (float) e.max(), false);
            else if (info.dataType == double.class) textField(info, Double::parseDouble, DECIMAL_ONLY, e.min(), e.max(), false);
            else if (info.dataType == String.class || info.dataType == Identifier.class) textField(info, String::length, null, Math.min(e.min(), 0), Math.max(e.max(), 1), true);
            else if (info.dataType == boolean.class) {
                Function<Object, Text> func = value -> Text.translatable((Boolean) value ? "gui.yes" : "gui.no").formatted((Boolean) value ? Formatting.GREEN : Formatting.RED);
                info.function = new AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Object, Text>>(button -> {
                    info.setValue(!(Boolean) info.value); button.setMessage(func.apply(info.value));
                }, func);
            } else if (info.dataType.isEnum()) {
                List<?> values = Arrays.asList(field.getType().getEnumConstants());
                Function<Object, Text> func = value -> Text.translatable(modid + ".midnightconfig." + "enum." + info.dataType.getSimpleName() + "." + info.toTemporaryValue());
                info.function = new AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Object, Text>>(button -> {
                    int index = values.indexOf(info.value) + 1;
                    info.value = values.get(index >= values.size() ? 0 : index); button.setMessage(func.apply(info.value));
                }, func);
            }} else if (c != null) {
            if (!c.requiredMod().isEmpty()) requiredModLoaded = NecPlatform.instance().isModLoaded(c.requiredMod());
            info.centered = c.centered();
        }
        if (requiredModLoaded) entries.add(info);
    }
    public static Class<?> getUnderlyingType(Field field) {
        if (field.getType() == List.class) {
            Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            try { return (Class<?>) listType.getField("TYPE").get(null);
            } catch (NoSuchFieldException | IllegalAccessException ignored) { return listType; }
        } else return field.getType();
    }
    public static Tooltip getTooltip(EntryInfo info, boolean isButton) {
        String key = info.modid + ".midnightconfig."+info.field.getName()+(!isButton ? ".label" : "" )+".tooltip";
        return Tooltip.of(isButton && info.error != null ? info.error : I18n.hasTranslation(key) ? Text.translatable(key) : Text.empty());
    }

    // TODO: Maybe move this into the screen class itself to free up some RAM?

    private static void textField(EntryInfo info, Function<String,Number> f, Pattern pattern, double min, double max, boolean cast) {
        boolean isNumber = pattern != null;
        info.function = (BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) (t, b) -> s -> {
            s = s.trim();
            if (!(s.isEmpty() || !isNumber || pattern.matcher(s).matches())) return false;

            Number value = 0; boolean inLimits = false; info.error = null;
            if (!(isNumber && s.isEmpty()) && !s.equals("-") && !s.equals(".")) {
                try { value = f.apply(s); } catch(NumberFormatException e){ return false; }
                inLimits = value.doubleValue() >= min && value.doubleValue() <= max;
                info.error = inLimits? null : Text.literal(value.doubleValue() < min ?
                        "§cMinimum " + (isNumber? "value" : "length") + (cast? " is " + (int)min : " is " + min) :
                        "§cMaximum " + (isNumber? "value" : "length") + (cast? " is " + (int)max : " is " + max)).formatted(Formatting.RED);
                t.setTooltip(getTooltip(info, true));
            }

            info.tempValue = s;
            t.setEditableColor(inLimits? 0xFFFFFFFF : 0xFFFF7777);
            info.inLimits = inLimits;
            b.active = entries.stream().allMatch(e -> e.inLimits);

            if (inLimits) {
                if (info.dataType == Identifier.class) info.setValue(Identifier.tryParse(s));
                else info.setValue(isNumber ? value : s);
            }

            if (info.field.getAnnotation(Entry.class).isColor()) {
                if (!s.contains("#")) s = '#' + s;
                if (!HEXADECIMAL_ONLY.matcher(s).matches()) return false;
                try { info.actionButton.setMessage(Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                } catch (Exception ignored) {}
            }
            return true;
        };
    }
    public static MidnightConfig getClass(String modid) {
        try { return configClass.get(modid).getDeclaredConstructor().newInstance(); } catch (Exception e) {throw new RuntimeException(e);}
    }
    public static void write(String modid) { getClass(modid).writeChanges(modid); }

    public void writeChanges(String modid) {
        try { if (!Files.exists(path = NecPlatform.instance().getConfigDirectory().resolve(modid + ".json"))) Files.createFile(path);
            Files.write(path, gson.toJson(getClass(modid)).getBytes());
        } catch (Exception e) { e.fillInStackTrace(); }
    }
    @Environment(EnvType.CLIENT)
    public static Screen getScreen(Screen parent, String modid) {
        return new MidnightConfigScreen(parent, modid);
    }
    @Environment(EnvType.CLIENT)
    public static class MidnightConfigScreen extends Screen {
        protected MidnightConfigScreen(Screen parent, String modid) {
            super(Text.translatable(modid + ".midnightconfig." + "title"));
            this.parent = parent; this.modid = modid;
            this.translationPrefix = modid + ".midnightconfig.";
            loadValues();

            for (EntryInfo e : entries) {
                if (e.modid.equals(modid)) {
                    String tabId = e.field.isAnnotationPresent(Entry.class) ? e.field.getAnnotation(Entry.class).category() : e.field.getAnnotation(Comment.class).category();
                    String name = translationPrefix + "category." + tabId;
                    if (!I18n.hasTranslation(name) && tabId.equals("default"))
                        name = translationPrefix + "title";
                    if (!tabs.containsKey(name)) {
                        Tab tab = new GridScreenTab(Text.translatable(name));
                        e.tab = tab;
                        tabs.put(name, tab);
                    } else e.tab = tabs.get(name);
                }
            }
            tabNavigation = TabNavigationWidget.builder(tabManager, this.width).tabs(tabs.values().toArray(new Tab[0])).build();
            tabNavigation.selectTab(0, false);
            tabNavigation.init();
            prevTab = tabManager.getCurrentTab();
        }
        public final String translationPrefix, modid;
        public final Screen parent;
        public MidnightConfigListWidget list;
        public TabManager tabManager = new TabManager(a -> {}, a -> {});
        public Map<String, Tab> tabs = new HashMap<>();
        public Tab prevTab;
        public TabNavigationWidget tabNavigation;
        public ButtonWidget done;
        public double scrollProgress = 0d;

        // Real Time config update //
        @Override
        public void tick() {
            super.tick();
            if (prevTab != null && prevTab != tabManager.getCurrentTab()) {
                prevTab = tabManager.getCurrentTab();
                this.list.clear(); fillList();
                list.setScrollY(0);
            }
            scrollProgress = list.getScrollY();
            for (EntryInfo info : entries) try {info.field.set(null, info.value);} catch (IllegalAccessException ignored) {}
            updateButtons();
        }
        public void updateButtons() {
            if (this.list != null) {
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1) {
                        if (entry.buttons.get(0) instanceof ClickableWidget widget)
                            if (widget.isFocused() || widget.isHovered()) widget.setTooltip(getTooltip(entry.info, true));
                        if (entry.buttons.get(1) instanceof ButtonWidget button)
                            button.active = !Objects.equals(entry.info.value.toString(), entry.info.defaultValue.toString());
                    }}}}
        public void loadValues() {
            try { gson.fromJson(Files.newBufferedReader(path), configClass.get(modid)); }
            catch (Exception e) { write(modid); }

            for (EntryInfo info : entries) {
                if (info.field.isAnnotationPresent(Entry.class))
                    try { info.value = info.field.get(null); info.tempValue = info.toTemporaryValue();
                    } catch (IllegalAccessException ignored) {}
            }
        }
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.tabNavigation.trySwitchTabsWithKey(keyCode)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        @Override
        public void close() {
            loadValues(); cleanup();
            Objects.requireNonNull(client).setScreen(parent);
        }
        private void cleanup() {
            entries.forEach(info -> {
                info.error = null; info.value = null; info.tempValue = null; info.actionButton = null; info.listIndex = 0; info.tab = null; info.inLimits = true;
            });
        }
        @Override
        public void init() {
            super.init();
            tabNavigation.setWidth(this.width); tabNavigation.init();
            if (tabs.size() > 1) this.addDrawableChild(tabNavigation);

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(this.width / 2 - 154, this.height - 26, 150, 20).build());
            done = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
                for (EntryInfo info : entries) if (info.modid.equals(modid)) try { info.field.set(null, info.value); } catch (IllegalAccessException ignored) {}
                write(modid); cleanup();
                Objects.requireNonNull(client).setScreen(parent);
            }).dimensions(this.width / 2 + 4, this.height - 26, 150, 20).build());

            this.list = new MidnightConfigListWidget(this.client, this.width, this.height - 57, 24, 25);
            this.addSelectableChild(this.list); fillList();
            if (tabs.size() > 1) list.renderHeaderSeparator = false;
        }
        public void fillList() {
            for (EntryInfo info : entries) {
                if (info.modid.equals(modid) && (info.tab == null || info.tab == tabManager.getCurrentTab())) {
                    Text name = Objects.requireNonNullElseGet(info.name, () -> Text.translatable(translationPrefix + info.field.getName()));
                    TextIconButtonWidget resetButton = TextIconButtonWidget.builder(Text.translatable("controls.reset"), (button -> {
                        info.value = info.defaultValue; info.listIndex = 0;
                        info.tempValue = info.toTemporaryValue();
                        list.clear(); fillList();
                    }), true).texture(Identifier.of("midnightlib","icon/reset"), 12, 12).dimension(20, 20).build();
                    resetButton.setPosition(width - 205 + 150 + 25, 0);

                    if (info.function != null) {
                        ClickableWidget widget;
                        Entry e = info.field.getAnnotation(Entry.class);

                        if (info.function instanceof Map.Entry) { // Enums & booleans
                            var values = (Map.Entry<ButtonWidget.PressAction, Function<Object, Text>>) info.function;
                            if (info.dataType.isEnum())
                                values.setValue(value -> Text.translatable(translationPrefix + "enum." + info.field.getType().getSimpleName() + "." + info.value.toString()));
                            widget = ButtonWidget.builder(values.getValue().apply(info.value), values.getKey()).dimensions(width - 185, 0, 150, 20).tooltip(getTooltip(info, true)).build();
                        }
                        else if (e.isSlider())
                            widget = new MidnightSliderWidget(width - 185, 0, 150, 20, Text.of(info.tempValue), (Double.parseDouble(info.tempValue) - e.min()) / (e.max() - e.min()), info);
                        else widget = new TextFieldWidget(textRenderer, width - 185, 0, 150, 20, Text.empty());

                        if (widget instanceof TextFieldWidget textField) {
                            textField.setMaxLength(info.width); textField.setText(info.tempValue);
                            Predicate<String> processor = ((BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) info.function).apply(textField, done);
                            textField.setTextPredicate(processor);
                        }
                        widget.setTooltip(getTooltip(info, true));

                        ButtonWidget cycleButton = null;
                        if (info.field.getType() == List.class) {
                            cycleButton = ButtonWidget.builder(Text.literal(String.valueOf(info.listIndex)).formatted(Formatting.GOLD), (button -> {
                                var values = (List<?>) info.value;
                                values.remove("");
                                info.listIndex = info.listIndex + 1;
                                if (info.listIndex > values.size()) info.listIndex = 0;
                                info.tempValue = info.toTemporaryValue();
                                if (info.listIndex == values.size()) info.tempValue = "";
                                list.clear(); fillList();
                            })).dimensions(width - 185, 0, 20, 20).build();
                        }
                        if (e.isColor()) {
                            ButtonWidget colorButton = ButtonWidget.builder(Text.literal("⬛"),
                                    button -> new Thread(() -> {
                                        Color newColor = JColorChooser.showDialog(null, Text.translatable("midnightconfig.colorChooser.title").getString(), Color.decode(!Objects.equals(info.tempValue, "") ? info.tempValue : "#FFFFFF"));
                                        if (newColor != null) {
                                            info.setValue("#" + Integer.toHexString(newColor.getRGB()).substring(2));
                                            list.clear(); fillList();
                                        }
                                    }).start()
                            ).dimensions(width - 185, 0, 20, 20).build();
                            try { colorButton.setMessage(Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                            } catch (Exception ignored) {}
                            info.actionButton = colorButton;
                        } else if (e.selectionMode() > -1) {
                            ButtonWidget explorerButton = TextIconButtonWidget.builder(Text.empty(),
                                    button -> new Thread(() -> {
                                        JFileChooser fileChooser = new JFileChooser(info.tempValue);
                                        fileChooser.setFileSelectionMode(e.selectionMode()); fileChooser.setDialogType(e.fileChooserType());
                                        fileChooser.setDialogTitle(Text.translatable(translationPrefix + info.field.getName() + ".fileChooser").getString());
                                        if ((e.selectionMode() == JFileChooser.FILES_ONLY || e.selectionMode() == JFileChooser.FILES_AND_DIRECTORIES) && Arrays.stream(e.fileExtensions()).noneMatch("*"::equals))
                                            fileChooser.setFileFilter(new FileNameExtensionFilter(
                                                    Text.translatable(translationPrefix + info.field.getName() + ".fileFilter").getString(), e.fileExtensions()));
                                        if (fileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                                            info.setValue(fileChooser.getSelectedFile().getAbsolutePath());
                                            list.clear(); fillList();
                                        }
                                    }).start(), true
                            ).texture(Identifier.of("midnightlib", "icon/explorer"), 12, 12).dimension(20, 20).build();
                            explorerButton.setPosition(width - 185, 0);
                            info.actionButton = explorerButton;
                        }
                        List<ClickableWidget> widgets = Lists.newArrayList(widget, resetButton);
                        if (info.actionButton != null) {
                            if (IS_SYSTEM_MAC) info.actionButton.active = false;
                            widget.setWidth(widget.getWidth() - 22); widget.setX(widget.getX() + 22);
                            widgets.add(info.actionButton);
                        } if (cycleButton != null) {
                            if (info.actionButton != null) info.actionButton.setX(info.actionButton.getX() + 22);
                            widget.setWidth(widget.getWidth() - 22); widget.setX(widget.getX() + 22);
                            widgets.add(cycleButton);
                        }
                        this.list.addButton(widgets, name, info);
                    } else this.list.addButton(List.of(), name, info);
                } list.setScrollY(scrollProgress);
                updateButtons();
            }
        }
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context,mouseX,mouseY,delta);
            this.list.render(context, mouseX, mouseY, delta);
            if (tabs.size() < 2) context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

            if (this.list != null) {
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1) {
                        if (entry.buttons.getFirst() instanceof ClickableWidget widget) {
                            int idMode = entry.info.field.getAnnotation(Entry.class).idMode();
                            if (idMode != -1) context.drawItem(idMode == 0 ? Registries.ITEM.get(Identifier.tryParse(entry.info.tempValue)).getDefaultStack() : Registries.BLOCK.get(Identifier.tryParse(entry.info.tempValue)).asItem().getDefaultStack(), widget.getX() + widget.getWidth() - 18, widget.getY() + 2);
                        }}}}}
    }
    @Environment(EnvType.CLIENT)
    public static class MidnightConfigListWidget extends ElementListWidget<ButtonEntry> {
        public boolean renderHeaderSeparator = true;
        public  MidnightConfigListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) { super(client, width, height, y, itemHeight); }
        @Override public int getScrollbarX() { return this.width -7; }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {
            if (renderHeaderSeparator) super.drawHeaderAndFooterSeparators(context);
            else { GlStateManager._enableBlend();
                context.drawTexture(RenderLayer::getGuiTextured, this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
                GlStateManager._disableBlend(); }
        }
        public void addButton(List<ClickableWidget> buttons, Text text, EntryInfo info) { this.addEntry(new ButtonEntry(buttons, text, info)); }
        public void clear() { this.clearEntries(); }
        @Override public int getRowWidth() { return 10000; }
    }
    public static class ButtonEntry extends ElementListWidget.Entry<ButtonEntry> {
        private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        public final Text text;
        public final List<ClickableWidget> buttons;
        public final EntryInfo info;
        public boolean centered = false;
        public MultilineTextWidget title;

        public ButtonEntry(List<ClickableWidget> buttons, Text text, EntryInfo info) {
            this.buttons = buttons; this.text = text; this.info = info;
            if (info != null) this.centered = info.centered;
            int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();

            if (text != null && (!text.getString().contains("spacer") || !buttons.isEmpty())) {
                title = new MultilineTextWidget((centered) ? (scaledWidth / 2 - (textRenderer.getWidth(text) / 2)) : 12, 0, Text.of(text), textRenderer);
                if (info != null) title.setTooltip(getTooltip(info, false));
                title.setMaxWidth(buttons.size() > 1 ? buttons.get(1).getX() - 24 : scaledWidth - 24);
            }
        }
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            buttons.forEach(b -> { b.setY(y); b.render(context, mouseX, mouseY, tickDelta);});
            if (title != null) {
                title.setY(y + 9);
                title.renderWidget(context, mouseX, mouseY, tickDelta);

                boolean tooltipVisible = mouseX >= title.getX() && mouseX < title.getWidth() + title.getX() && mouseY >= title.getY() && mouseY < title.getHeight() + title.getY();
                if (tooltipVisible && title.getTooltip() != null) context.drawOrderedTooltip(textRenderer, title.getTooltip().getLines(MinecraftClient.getInstance()), mouseX, mouseY);
            }
        }
        public List<? extends Element> children() {return Lists.newArrayList(buttons);}
        public List<? extends Selectable> selectableChildren() {return Lists.newArrayList(buttons);}
    }
    public static class MidnightSliderWidget extends SliderWidget {
        private final EntryInfo info; private final Entry e;
        public MidnightSliderWidget(int x, int y, int width, int height, Text text, double value, EntryInfo info) {
            super(x, y, width, height, text, value);
            this.e = info.field.getAnnotation(Entry.class);
            this.info = info;
        }

        @Override
        public void updateMessage() { this.setMessage(Text.of(info.tempValue)); }

        @Override
        public void applyValue() {
            if (info.dataType == int.class) info.setValue(((Number) (e.min() + value * (e.max() - e.min()))).intValue());
            else if (info.field.getType() == double.class) info.setValue(Math.round((e.min() + value * (e.max() - e.min())) * (double) e.precision()) / (double) e.precision());
            else if (info.field.getType() == float.class) info.setValue(Math.round((e.min() + value * (e.max() - e.min())) * (float) e.precision()) / (float) e.precision());
        }
    }

    /**
     * Entry Annotation<br>
     * - <b>width</b>: The maximum character length of the {@link String}, {@link Identifier} or String/Identifier {@link List<>} field<br>
     * - <b>min</b>: The minimum value of the <code>int</code>, <code>float</code> or <code>double</code> field<br>
     * - <b>max</b>: The maximum value of the <code>int</code>, <code>float</code> or <code>double</code> field<br>
     * - <b>name</b>: The name of the field in the config screen<br>
     * - <b>selectionMode</b>: The selection mode of the file picker button for {@link String} fields,
     *   -1 for none, {@link JFileChooser#FILES_ONLY} for files, {@link JFileChooser#DIRECTORIES_ONLY} for directories,
     *   {@link JFileChooser#FILES_AND_DIRECTORIES} for both (default: -1). Remember to set the translation key
     *   <code>[modid].midnightconfig.[fieldName].fileChooser.title</code> for the file picker dialog title<br>
     * - <b>fileChooserType</b>: The type of the file picker button for {@link String} fields,
     * can be {@link JFileChooser#OPEN_DIALOG} or {@link JFileChooser#SAVE_DIALOG} (default: {@link JFileChooser#OPEN_DIALOG}).
     * Remember to set the translation key <code>[modid].midnightconfig.[fieldName].fileFilter.description</code> for the file filter description
     * if <code>"*"</code> is not used as file extension<br>
     * - <b>fileExtensions</b>: The file extensions for the file picker button for {@link String} fields (default: <code>{"*"}</code>),
     *  only works if selectionMode is {@link JFileChooser#FILES_ONLY} or {@link JFileChooser#FILES_AND_DIRECTORIES}<br>
     * - <b>isColor</b>: If the field is a hexadecimal color code (default: false)<br>
     * - <b>isSlider</b>: If the field is a slider (default: false)<br>
     * - <b>precision</b>: The precision of the <code>float</code> or <code>double</code> field (default: 100)<br>
     * - <b>category</b>: The category of the field in the config screen (default: "default")<br>
     * */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Entry {
        int width() default 400;
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        String name() default "";
        int selectionMode() default -1;        // -1 for none, 0 for file, 1 for directory, 2 for both
        int fileChooserType() default JFileChooser.OPEN_DIALOG;
        String[] fileExtensions() default {"*"};
        int idMode() default -1;               // -1 for none, 0 for item, 1 for block
        boolean isColor() default false;
        boolean isSlider() default false;
        int precision() default 100;
        String category() default "default";
        String requiredMod() default "";
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Client {}

    /**
     * Hides the entry in config screens, but still makes it
     * accessible through the command {@code /midnightconfig MOD_ID ENTRY} and directly editing the config file.
     */
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Server {}

    /**
     * Hides the entry entirely.
     * Accessible only through directly editing the config file.
     * Perfect for saving persistent internal data.
     */
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Hidden {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment {
        boolean centered() default false;
        String category() default "default";
        String requiredMod() default "";
    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) { return false; }
        public boolean shouldSkipField(FieldAttributes fieldAttributes) { return fieldAttributes.getAnnotation(Entry.class) == null; }
    }
}