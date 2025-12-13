package fudge.notenoughcrashes.config;

import com.google.gson.*;
import com.google.gson.stream.*;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Color;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** MidnightConfig by Martin "Motschen" Prokoph
 *  Minimalist config library - feel free to copy!
 *  Originally based on <a href="https://github.com/Minenash/TinyConfig">...</a>
 *  Credits to Minenash */

@SuppressWarnings("unchecked")
public abstract class MidnightConfig {
    private static final Pattern INTEGER_ONLY = Pattern.compile("(-?[0-9]*)");
    private static final Pattern DECIMAL_ONLY = Pattern.compile("-?(\\d+\\.?\\d*|\\d*\\.?\\d+|\\.)");
    private static final Pattern HEXADECIMAL_ONLY = Pattern.compile("(-?[#0-9a-fA-F]*)");
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).excludeFieldsWithModifiers(Modifier.PRIVATE).excludeFieldsWithModifiers(Modifier.FINAL)
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                public boolean shouldSkipClass(Class<?> clazz) { return false; }
                public boolean shouldSkipField(FieldAttributes fieldAttributes) { return fieldAttributes.getAnnotation(Entry.class) == null; }
            })
            .registerTypeAdapter(Identifier.class, new TypeAdapter<Identifier>() {
                public void write(JsonWriter out, Identifier id) throws IOException { out.value(id.toString()); }
                public Identifier read(JsonReader in) throws IOException { return Identifier.of(in.nextString()); }
            }).setPrettyPrinting().create();

    protected static final LinkedHashMap<String, EntryInfo> entries = new LinkedHashMap<>();    // modid:fieldName -> EntryInfo

    public static final Map<String, MidnightConfig> configInstances = new HashMap<>();

    protected String modid;
    protected boolean reloadScreen = false;
    public Class<? extends MidnightConfig> configClass;

    public static <T extends MidnightConfig> T createInstance(String modid, Class<? extends MidnightConfig> configClass) { // This is basically an argumented constructor without the requirement of having one in each config class
        try {
            T instance = (T) configClass.getDeclaredConstructor().newInstance();
            instance.modid = modid;
            instance.configClass = configClass;
            configInstances.put(modid, instance);
            return instance;
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void init(String modid, Class<? extends MidnightConfig> config) {
        MidnightConfig instance = createInstance(modid, config);

        for (Field field : config.getFields()) {
            //noinspection ConstantValue
            if ((field.isAnnotationPresent(Entry.class) || field.isAnnotationPresent(Comment.class))
                    && !field.isAnnotationPresent(Server.class)
                    && !field.isAnnotationPresent(Hidden.class)
                    && NecPlatform.instance().isClientEnv())
                instance.addClientEntry(field, new EntryInfo(field, modid));
        }
        instance.loadValuesFromJson();
    }

    public void addClientEntry(Field field, EntryInfo info) {
        Entry e = info.entry;
        if (e != null && info.dataType != null) {
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
                Function<Object, Text> func = value -> getEnumTranslatableText(value, info);
                info.function = new AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Object, Text>>(button -> {
                    int index = values.indexOf(info.value) + 1;
                    info.setValue(values.get(index >= values.size() ? 0 : index));
                    button.setMessage(func.apply(info.value));
                }, func);
            }

            try { info.defaultValue = field.get(null);
            } catch (IllegalAccessException ignored) {}
        }
        entries.put(modid + ":" + field.getName(), info);
    }

    public static Class<?> getUnderlyingType(Field field) {
        Class<?> rawType = field.getType();
        if (field.getType() == List.class)
            rawType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        try { return (Class<?>) rawType.getField("TYPE").get(null); // Tries to get primitive types from non-primitives (e.g. Boolean -> boolean)
        } catch (NoSuchFieldException | IllegalAccessException ignored) { return rawType; }
    }

    private static void textField(EntryInfo info, Function<String,Number> f, Pattern pattern, double min, double max, boolean cast) {
        boolean isNumber = pattern != null;
        info.function = (BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>>) (t, b) -> s -> {
            s = s.trim();
            if (!(s.isEmpty() || !isNumber || pattern.matcher(s).matches()) ||
                    (info.dataType == Identifier.class && Identifier.validate(s).isError())) return false;

            Number value = 0; boolean inLimits = false; info.error = null;
            if (!(isNumber && s.isEmpty()) && !s.equals("-") && !s.equals(".")) {
                try { value = f.apply(s); } catch(NumberFormatException e){ return false; }
                inLimits = value.doubleValue() >= min && value.doubleValue() <= max;
                info.error = inLimits? null : Text.literal(value.doubleValue() < min ?
                        "§cMinimum " + (isNumber? "value" : "length") + (cast? " is " + (int)min : " is " + min) :
                        "§cMaximum " + (isNumber? "value" : "length") + (cast? " is " + (int)max : " is " + max)).formatted(Formatting.RED);
                t.setTooltip(info.getTooltip(true));
            }

            info.tempValue = s;
            t.setEditableColor(inLimits? 0xFFFFFFFF : 0xFFFF7777);
            info.inLimits = inLimits;
            b.active = entries.values().stream().allMatch(e -> e.inLimits);

            if (inLimits) {
                if (info.dataType == Identifier.class)
                    info.setValue(Identifier.tryParse(s));
                else info.setValue(isNumber ? value : s);
            }

            if (info.entry.isColor()) {
                if (!s.contains("#")) s = '#' + s;
                if (!HEXADECIMAL_ONLY.matcher(s).matches()) return false;
                try { info.actionButton.setMessage(Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                } catch (Exception ignored) {}
            }
            return true;
        };
    }

    protected Text getEnumTranslatableText(Object value, EntryInfo info) {
        String translationKey = "%s.midnightconfig.enum.%s.%s".formatted(modid, info.dataType.getSimpleName(), info.toTemporaryValue());
        return I18n.hasTranslation(translationKey) ? Text.translatable(translationKey) : Text.literal(info.toTemporaryValue());
    }

    public void loadValuesFromJson() {
        try {
            gson.fromJson(Files.newBufferedReader(getJsonFilePath()), configClass);
        } catch (Exception e) {
            write(modid);
        }

        entries.values().forEach(info -> {
            if (info.field != null && info.entry != null) {
                try {
                    info.value = info.field.get(null) == null ? info.defaultValue : info.field.get(null);
                    info.tempValue = info.toTemporaryValue();
                    info.updateConditions();
                } catch (IllegalAccessException ignored) {}
            }
        });
    }

    public static void write(String modid) {
        configInstances.get(modid).writeChanges(modid);
    }

    @Deprecated
    public void writeChanges(String modid) {
        this.writeChanges();
    }

    public void writeChanges() {
        try {
            Path path;
            if (!Files.exists(path = getJsonFilePath()))
                Files.createFile(path);
            Files.write(path, gson.toJson(this).getBytes());
        } catch (Exception e) { e.fillInStackTrace(); }
    }

    public Path getJsonFilePath() {
        return NecPlatform.instance().getConfigDirectory().resolve(modid + ".json");
    }

    @SuppressWarnings("unused") // Utility for mod authors
    public static @Nullable Object getDefaultValue(String modid, String entry) {
        String key = modid + ":" + entry;
        return entries.containsKey(key) ? entries.get(key).defaultValue : null;
    }

    // Overridable method
    public void onTabInit(String tabName, MidnightConfigListWidget list, MidnightConfigScreen screen) {
    }

    public static MidnightConfigScreen getScreen(Screen parent, String modid) {
        return configInstances.get(modid).getScreen(parent);
    }
    public MidnightConfigScreen getScreen(Screen parent) {
        return new MidnightConfigScreen(parent, modid);
    }

    /**
     * Entry Annotation<br>
     * - <b>width</b>: The maximum character length of the {@link String}, {@link Identifier} or String/Identifier {@link List<>} field<br>
     * - <b>min</b>: The minimum value of the <code>int</code>, <code>float</code> or <code>double</code> field<br>
     * - <b>max</b>: The maximum value of the <code>int</code>, <code>float</code> or <code>double</code> field<br>
     * - <b>name</b>: Will be used instead of the default translation key, if not empty<br>
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
        @Deprecated String requiredMod() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Client {}

    /**
     * Hides the entry in config screens, but still makes it accessible through the command {@code /midnightconfig MOD_ID ENTRY} and directly editing the config file.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Server {}

    /**
     * Hides the entry entirely.
     * Accessible only through directly editing the config file.
     * Perfect for saving persistent internal data.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Hidden {}

    /**
     * Comment Annotation<br>
     * - <b>{@link Comment#centered()}</b>: If the comment should be centered<br>
     * - <b>{@link Comment#category()}</b>: The category of the comment in the config screen<br>
     * - <b>{@link Comment#name()}</b>: Will be used instead of the default translation key, if not empty<br>
     * - <b>{@link Comment#url()}</b>: The url of the comment should link to in the config screen (none if left empty)<br>
     * */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Comment {
        boolean centered() default false;
        String category() default "default";
        String name() default "";
        String url() default "";
        @Deprecated String requiredMod() default "";
    }
    /**
     * Condition Annotation<br>
     * - <b>{@link Condition#requiredModId()}</b>: The id of a mod that is required to be loaded.<br>
     * - <b>{@link Condition#requiredOption()}</b>: The {@link Field} which will be used to check the condition. Can also access options of other MidnightLib mods ("modid:optionName").<br>
     * - <b>{@link Condition#requiredValue()}</b>: The value that {@link Condition#requiredOption()} should be set to for the condition to be met.<br>
     * - <b>{@link Condition#visibleButLocked()}</b>: The behaviour to take when {@link Condition#requiredModId} is not loaded
     *   or {@link Condition#requiredOption()} returns a value that is not {@link Condition#requiredValue()}.<br>
     *   <code>true</code> – Option is visible, but not editable<br>
     *   <code>false</code> – Option is completely hidden
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Conditions.class)
    @Target(ElementType.FIELD)
    public @interface Condition {
        String requiredModId() default "";
        String requiredOption() default "";
        String[] requiredValue() default {"true"};
        boolean visibleButLocked() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Conditions {
        Condition[] value();
    }
}