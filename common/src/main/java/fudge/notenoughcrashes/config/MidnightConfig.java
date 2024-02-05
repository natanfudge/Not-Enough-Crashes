package fudge.notenoughcrashes.config;
import com.google.common.collect.Lists;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** MidnightConfig v2.5.1 by TeamMidnightDust & Motschen
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
        Object widget;
        int width;
        boolean centered;
        Text error;
        Object defaultValue;
        Object value;
        String tempValue;
        boolean inLimits = true;
        String id;
        Text name;
        int index;
        ClickableWidget colorButton;
        Tab tab;
    }

    public static final Map<String, Class<? extends MidnightConfig>> configClass = new HashMap<>();
    private static Path path;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).excludeFieldsWithModifiers(Modifier.PRIVATE).addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy()).setPrettyPrinting().create();

    public static void init(String modid, Class<? extends MidnightConfig> config) {
        path = NecPlatform.instance().getConfigDirectory().resolve(modid + ".json");
        configClass.put(modid, config);

        for (Field field : config.getFields()) {
            EntryInfo info = new EntryInfo();
            if ((field.isAnnotationPresent(Entry.class) || field.isAnnotationPresent(Comment.class)) && !field.isAnnotationPresent(Server.class) && !field.isAnnotationPresent(Hidden.class) && NecPlatform.instance().isClient())
                initClient(modid, field, info);
            if (field.isAnnotationPresent(Comment.class)) info.centered = field.getAnnotation(Comment.class).centered();
            if (field.isAnnotationPresent(Entry.class))
                try {
                    info.defaultValue = field.get(null);
                } catch (IllegalAccessException ignored) {}
        }
        try { gson.fromJson(Files.newBufferedReader(path), config); }
        catch (Exception e) { write(modid); }

        for (EntryInfo info : entries) {
            if (info.field.isAnnotationPresent(Entry.class))
                try {
                    info.value = info.field.get(null);
                    info.tempValue = info.value.toString();
                } catch (IllegalAccessException ignored) {}
        }
    }
    @Environment(EnvType.CLIENT)
    private static void initClient(String modid, Field field, EntryInfo info) {
        Class<?> type = field.getType();
        Entry e = field.getAnnotation(Entry.class);
        info.width = e != null ? e.width() : 0;
        info.field = field;
        info.id = modid;

        if (e != null) {
            if (!e.name().isEmpty()) info.name = Text.translatable(e.name());
            if (type == int.class) textField(info, Integer::parseInt, INTEGER_ONLY, (int) e.min(), (int) e.max(), true);
            else if (type == float.class) textField(info, Float::parseFloat, DECIMAL_ONLY, (float) e.min(), (float) e.max(), false);
            else if (type == double.class) textField(info, Double::parseDouble, DECIMAL_ONLY, e.min(), e.max(), false);
            else if (type == String.class || type == List.class) textField(info, String::length, null, Math.min(e.min(), 0), Math.max(e.max(), 1), true);
            else if (type == boolean.class) {
                Function<Object, Text> func = value -> Text.translatable((Boolean) value ? "gui.yes" : "gui.no").formatted((Boolean) value ? Formatting.GREEN : Formatting.RED);
                info.widget = new AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Object, Text>>(button -> {
                    info.value = !(Boolean) info.value;
                    button.setMessage(func.apply(info.value));
                }, func);
            } else if (type.isEnum()) {
                List<?> values = Arrays.asList(field.getType().getEnumConstants());
                Function<Object, Text> func = value -> Text.translatable(modid + ".midnightconfig." + "enum." + type.getSimpleName() + "." + info.value.toString());
                info.widget = new AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Object, Text>>(button -> {
                    int index = values.indexOf(info.value) + 1;
                    info.value = values.get(index >= values.size() ? 0 : index);
                    button.setMessage(func.apply(info.value));
                }, func);
            }
        }
        entries.add(info);
    }
    public static Tooltip getTooltip(EntryInfo info) {
        String key = info.id + ".midnightconfig."+info.field.getName()+".tooltip";
        return Tooltip.of(info.error != null ? info.error : I18n.hasTranslation(key) ? Text.translatable(key) : Text.empty());
    }

    private static void textField(EntryInfo info, Function<String,Number> f, Pattern pattern, double min, double max, boolean cast) {
        boolean isNumber = pattern != null;
        info.widget = (BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) (t, b) -> s -> {
            s = s.trim();
            if (!(s.isEmpty() || !isNumber || pattern.matcher(s).matches())) return false;

            Number value = 0;
            boolean inLimits = false;
            info.error = null;
            if (!(isNumber && s.isEmpty()) && !s.equals("-") && !s.equals(".")) {
                try { value = f.apply(s); } catch(NumberFormatException e){ return false; }
                inLimits = value.doubleValue() >= min && value.doubleValue() <= max;
                info.error = inLimits? null : Text.literal(value.doubleValue() < min ?
                        "§cMinimum " + (isNumber? "value" : "length") + (cast? " is " + (int)min : " is " + min) :
                        "§cMaximum " + (isNumber? "value" : "length") + (cast? " is " + (int)max : " is " + max)).formatted(Formatting.RED);
                t.setTooltip(getTooltip(info));
            }

            info.tempValue = s;
            t.setEditableColor(inLimits? 0xFFFFFFFF : 0xFFFF7777);
            info.inLimits = inLimits;
            b.active = entries.stream().allMatch(e -> e.inLimits);

            if (inLimits && info.field.getType() != List.class)
                info.value = isNumber? value : s;
            else if (inLimits) {
                if (((List<String>) info.value).size() == info.index) ((List<String>) info.value).add("");
                ((List<String>) info.value).set(info.index, Arrays.stream(info.tempValue.replace("[", "").replace("]", "").split(", ")).toList().get(0));
            }

            if (info.field.getAnnotation(Entry.class).isColor()) {
                if (!s.contains("#")) s = '#' + s;
                if (!HEXADECIMAL_ONLY.matcher(s).matches()) return false;
                try {
                    info.colorButton.setMessage(Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                } catch (Exception ignored) {}
            }
            return true;
        };
    }
    public static MidnightConfig getClass(String modid) {
        try { return configClass.get(modid).getDeclaredConstructor().newInstance(); } catch (Exception e) {throw new RuntimeException(e);}
    }
    public static void write(String modid) {
        getClass(modid).writeChanges(modid);
    }

    public void writeChanges(String modid) {
        path = NecPlatform.instance().getConfigDirectory().resolve(modid + ".json");
        try {
            if (!Files.exists(path)) Files.createFile(path);
            Files.write(path, gson.toJson(getClass(modid)).getBytes());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
    @Environment(EnvType.CLIENT)
    public static Screen getScreen(Screen parent, String modid) {
        return new MidnightConfigScreen(parent, modid);
    }
    @Environment(EnvType.CLIENT)
    public static class MidnightConfigScreen extends Screen {
        protected MidnightConfigScreen(Screen parent, String modid) {
            super(Text.translatable(modid + ".midnightconfig." + "title"));
            this.parent = parent;
            this.modid = modid;
            this.translationPrefix = modid + ".midnightconfig.";
            loadValues();

            for (EntryInfo e : entries) {
                if (e.id.equals(modid)) {
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
        public final String translationPrefix;
        public final Screen parent;
        public final String modid;
        public MidnightConfigListWidget list;
        public boolean reload = false;
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
                this.list.clear();
                fillList();
                list.setScrollAmount(0);
            }
            scrollProgress = list.getScrollAmount();
            for (EntryInfo info : entries) {
                try {info.field.set(null, info.value);} catch (IllegalAccessException ignored) {}
            }
            updateResetButtons();
        }
        public void updateResetButtons() {
            if (this.list != null) {
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1 && entry.buttons.get(1) instanceof ButtonWidget button) {
                        button.active = !Objects.equals(entry.info.value.toString(), entry.info.defaultValue.toString());
                    }
                }
            }
        }
        public void loadValues() {
            try { gson.fromJson(Files.newBufferedReader(path), configClass.get(modid)); }
            catch (Exception e) { write(modid); }

            for (EntryInfo info : entries) {
                if (info.field.isAnnotationPresent(Entry.class))
                    try {
                        info.value = info.field.get(null);
                        info.tempValue = info.value.toString();
                    } catch (IllegalAccessException ignored) {}
            }
        }
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.tabNavigation.trySwitchTabsWithKey(keyCode)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        @Override
        public void init() {
            super.init();
            tabNavigation.setWidth(this.width);
            tabNavigation.init();
            if (tabs.size() > 1) this.addDrawableChild(tabNavigation);

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
                loadValues();
                Objects.requireNonNull(client).setScreen(parent);
            }).dimensions(this.width / 2 - 154, this.height - 28, 150, 20).build());
            done = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
                for (EntryInfo info : entries)
                    if (info.id.equals(modid)) {
                        try {
                            info.field.set(null, info.value);
                        } catch (IllegalAccessException ignored) {}
                    }
                write(modid);
                Objects.requireNonNull(client).setScreen(parent);
            }).dimensions(this.width / 2 + 4, this.height - 28, 150, 20).build());

            this.list = new MidnightConfigListWidget(this.client, this.width, this.height - 64, 32, 25);
            if (this.client != null && this.client.world != null) this.list.setRenderBackground(false);
            this.addSelectableChild(this.list);

            fillList();
            reload = true;
        }
        public void fillList() {
            for (EntryInfo info : entries) {
                if (info.id.equals(modid) && (info.tab == null || info.tab == tabManager.getCurrentTab())) {
                    Text name = Objects.requireNonNullElseGet(info.name, () -> Text.translatable(translationPrefix + info.field.getName()));
                    TextIconButtonWidget resetButton = TextIconButtonWidget.builder(Text.translatable("controls.reset"), (button -> {
                        info.value = info.defaultValue;
                        info.tempValue = info.defaultValue.toString();
                        info.index = 0;
                        list.clear();
                        fillList();
                    }), true).texture(new Identifier(NotEnoughCrashes.MOD_ID,"icon/reset"), 12, 12).dimension(40, 20).build();
                    resetButton.setPosition(width - 205, 0);

                    if (info.widget instanceof Map.Entry) {
                        Map.Entry<ButtonWidget.PressAction, Function<Object, Text>> widget = (Map.Entry<ButtonWidget.PressAction, Function<Object, Text>>) info.widget;
                        if (info.field.getType().isEnum())
                            widget.setValue(value -> Text.translatable(translationPrefix + "enum." + info.field.getType().getSimpleName() + "." + info.value.toString()));
                        this.list.addButton(List.of(ButtonWidget.builder(widget.getValue().apply(info.value), widget.getKey()).dimensions(width - 160, 0, 150, 20).tooltip(getTooltip(info)).build(), resetButton), name, info);
                    } else if (info.field.getType() == List.class) {
                        if (!reload) info.index = 0;
                        TextFieldWidget widget = new TextFieldWidget(textRenderer, width - 160, 0, 150, 20, Text.empty());
                        widget.setMaxLength(info.width);
                        if (info.index < ((List<String>) info.value).size())
                            widget.setText((String.valueOf(((List<String>) info.value).get(info.index))));
                        Predicate<String> processor = ((BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) info.widget).apply(widget, done);
                        widget.setTextPredicate(processor);
                        resetButton.setWidth(20);
                        ButtonWidget cycleButton = ButtonWidget.builder(Text.literal(String.valueOf(info.index)).formatted(Formatting.GOLD), (button -> {
                            if (((List<?>) info.value).contains("")) ((List<String>) info.value).remove("");
                            info.index = info.index + 1;
                            if (info.index > ((List<String>) info.value).size()) info.index = 0;
                            list.clear();
                            fillList();
                        })).dimensions(width - 185, 0, 20, 20).build();
                        widget.setTooltip(getTooltip(info));
                        this.list.addButton(List.of(widget, resetButton, cycleButton), name, info);
                    } else if (info.widget != null) {
                        ClickableWidget widget;
                        Entry e = info.field.getAnnotation(Entry.class);
                        if (e.isSlider())
                            widget = new MidnightSliderWidget(width - 160, 0, 150, 20, Text.of(info.tempValue), (Double.parseDouble(info.tempValue) - e.min()) / (e.max() - e.min()), info);
                        else
                            widget = new TextFieldWidget(textRenderer, width - 160, 0, 150, 20, null, Text.of(info.tempValue));
                        if (widget instanceof TextFieldWidget textField) {
                            textField.setMaxLength(info.width);
                            textField.setText(info.tempValue);
                            Predicate<String> processor = ((BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) info.widget).apply(textField, done);
                            textField.setTextPredicate(processor);
                        }
                        widget.setTooltip(getTooltip(info));
                        if (e.isColor()) {
                            resetButton.setWidth(20);
                            ButtonWidget colorButton = ButtonWidget.builder(Text.literal("⬛"), (button -> {
                            })).dimensions(width - 185, 0, 20, 20).build();
                            try {
                                colorButton.setMessage(Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                            } catch (Exception ignored) {}
                            info.colorButton = colorButton;
                            colorButton.active = false;
                            this.list.addButton(List.of(widget, resetButton, colorButton), name, info);
                        } else this.list.addButton(List.of(widget, resetButton), name, info);
                    } else {
                        this.list.addButton(List.of(), name, info);
                    }
                }
                list.setScrollAmount(scrollProgress);
                updateResetButtons();
            }
        }
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (client != null && client.world != null) super.renderInGameBackground(context);
            this.list.render(context, mouseX, mouseY, delta);
            super.render(context,mouseX,mouseY,delta);

            if (tabs.size() < 2) context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        }
        @Override public void renderBackground(DrawContext c, int x, int y, float d) {}
    }
    @Environment(EnvType.CLIENT)
    public static class MidnightConfigListWidget extends ElementListWidget<ButtonEntry> {
        public MidnightConfigListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
        }
        @Override
        public int getScrollbarPositionX() { return this.width -7; }

        public void addButton(List<ClickableWidget> buttons, Text text, EntryInfo info) {
            this.addEntry(new ButtonEntry(buttons, text, info));
        }
        public void clear() { this.clearEntries(); }
        @Override
        public int getRowWidth() { return 10000; }
        @Override
        protected void renderDecorations(DrawContext c, int mouseX, int mouseY) {
            c.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
            c.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.getX(), 0, 0.0F, 0.0F, this.width, this.getY(), 32, 32);
            c.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.getX(), this.getBottom(), 0.0F, 0.0F, this.width, this.height, 32, 32);
            c.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (client == null || client.world == null) return;
            c.fillGradient(RenderLayer.getGuiOverlay(), this.getX(), this.getY(), this.getRight(), this.getY() + 4, -16777216, 0, 0);
            c.fillGradient(RenderLayer.getGuiOverlay(), this.getX(), this.getBottom() - 4, this.getRight(), this.getBottom(), 0, -16777216, 0);
        }
    }
    public static class ButtonEntry extends ElementListWidget.Entry<ButtonEntry> {
        private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        public final List<ClickableWidget> buttons;
        private final Text text;
        public final EntryInfo info;
        public static final Map<ClickableWidget, Text> buttonsWithText = new HashMap<>();

        private ButtonEntry(List<ClickableWidget> buttons, Text text, EntryInfo info) {
            if (!buttons.isEmpty()) buttonsWithText.put(buttons.get(0),text);
            this.buttons = buttons;
            this.text = text;
            this.info = info;
        }
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            buttons.forEach(b -> { b.setY(y); b.render(context, mouseX, mouseY, tickDelta); });
            if (text != null && (!text.getString().contains("spacer") || !buttons.isEmpty())) {
                if (info.centered) context.drawTextWithShadow(textRenderer, text, MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - (textRenderer.getWidth(text) / 2), y + 5, 0xFFFFFF);
                else {
                    int wrappedY = y;
                    for(Iterator<OrderedText> iterator = textRenderer.wrapLines(text, (buttons.size() > 1 ? buttons.get(1).getX()-24 : MinecraftClient.getInstance().getWindow().getScaledWidth() - 24)).iterator(); iterator.hasNext(); wrappedY += 9) {
                        OrderedText orderedText = iterator.next();
                        context.drawTextWithShadow(textRenderer, orderedText, 12, wrappedY + 5, 0xFFFFFF);
                    }
                }
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
        protected void updateMessage() {
            this.setMessage(Text.of(info.tempValue));
        }

        @Override
        protected void applyValue() {
            if (info.field.getType() == int.class) info.value = ((Number) (e.min() + value * (e.max() - e.min()))).intValue();
            else if (info.field.getType() == double.class) info.value = Math.round((e.min() + value * (e.max() - e.min())) * (double) e.precision()) / (double) e.precision();
            else if (info.field.getType() == float.class) info.value = Math.round((e.min() + value * (e.max() - e.min())) * (float) e.precision()) / (float) e.precision();
            info.tempValue = String.valueOf(info.value);
        }
    }
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Entry {
        int width() default 100;
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        String name() default "";
        boolean isColor() default false;
        boolean isSlider() default false;
        int precision() default 100;
        String category() default "default";
    }
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Client {}
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Server {}
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Hidden {}
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment {
        boolean centered() default false;
        String category() default "default";
    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) { return false; }
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Entry.class) == null;
        }
    }
}