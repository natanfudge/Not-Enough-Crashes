package fudge.notenoughcrashes.config;

import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.List;

public class EntryInfo {
    public MidnightConfig.Entry entry;
    public MidnightConfig.Comment comment;
    public MidnightConfig.Condition[] conditions;
    public final Field field;
    public final Class<?> dataType;
    public final String modid, fieldName, translationKey;
    int listIndex;
    Object defaultValue, value, function;
    String tempValue;   // The value visible in the config screen
    boolean inLimits = true;
    Text error;
    ClickableWidget actionButton; // color picker button / explorer button
    Tab tab;
    boolean conditionsMet = true;

    public EntryInfo(Field field, String modid) {
        this.field = field;
        this.modid = modid;
        if (field != null) {
            this.fieldName = field.getName();
            this.dataType = MidnightConfig.getUnderlyingType(field);
            this.entry = field.getAnnotation(MidnightConfig.Entry.class);
            this.comment = field.getAnnotation(MidnightConfig.Comment.class);
            this.conditions = field.getAnnotationsByType(MidnightConfig.Condition.class);
        } else {
            this.fieldName = "";
            this.dataType = null;
        }

        if (entry != null && !entry.name().isEmpty())
            this.translationKey = entry.name();
        else if (comment != null && !comment.name().isEmpty())
            this.translationKey = comment.name();
        else this.translationKey = modid + ".midnightconfig." + fieldName;
    }

    public void setValue(Object value) {
        if (this.field.getType() != List.class) {
            this.value = value;
            this.tempValue = value.toString();
        } else {
            writeList(this.listIndex, value);
            this.tempValue = toTemporaryValue();
        }
    }

    public String toTemporaryValue() {
        if (this.field.getType() != List.class) return this.value.toString();
        else try {
            return ((List<?>) this.value).get(this.listIndex).toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    public void updateFieldValue() {
        try {
            if (this.field.get(null) != value) MidnightConfig.entries.values().forEach(EntryInfo::updateConditions);
            this.field.set(null, this.value);
        } catch (IllegalAccessException ignored) {
        }
    }

    public void updateConditions() {
        boolean prevConditionState = this.conditionsMet;
        if (this.conditions.length > 0) this.conditionsMet = true;    // reset conditions
        for (MidnightConfig.Condition condition : this.conditions) {
            //noinspection ConstantValue
            if (!condition.requiredModId().isEmpty() && !NecPlatform.instance().isModLoaded(condition.requiredModId()))
                this.conditionsMet = false;
            String requiredOption = condition.requiredOption().contains(":") ? condition.requiredOption() : (this.modid + ":" + condition.requiredOption());
            if (MidnightConfig.entries.get(requiredOption) instanceof EntryInfo info)
                this.conditionsMet &= List.of(condition.requiredValue()).contains(info.tempValue);
            if (!this.conditionsMet) break;
        }
        if (prevConditionState != this.conditionsMet) MidnightConfig.configInstances.get(modid).reloadScreen = true;
    }

    public <T> void writeList(int index, T value) {
        //noinspection unchecked
        var list = (List<T>) this.value;
        if (index >= list.size())
            list.add(value);
        else list.set(index, value);
    }

    public Tooltip getTooltip(boolean isButton) {
        String key = translationKey + (!isButton ? ".label" : "") + ".tooltip";
        return Tooltip.of(isButton && this.error != null ? this.error : I18n.hasTranslation(key) ? Text.translatable(key) : Text.empty());
    }
}