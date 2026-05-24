package fudge.notenoughcrashes.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class MidnightSliderWidget extends AbstractSliderButton {
    private final EntryInfo info;
    private final MidnightConfig.Entry e;

    public MidnightSliderWidget(int x, int y, int width, int height, Component text, double value, EntryInfo info) {
        super(x, y, width, height, text, value);
        this.e = info.entry;
        this.info = info;
    }

    @Override
    public void updateMessage() {
        this.setMessage(Component.literal(info.tempValue));
    }

    @Override
    public void applyValue() {
        if (info.dataType == int.class) info.setValue(((Number) (e.min() + value * (e.max() - e.min()))).intValue());
        else if (info.dataType == double.class)
            info.setValue(Math.round((e.min() + value * (e.max() - e.min())) * (double) e.precision()) / (double) e.precision());
        else if (info.dataType == float.class)
            info.setValue(Math.round((e.min() + value * (e.max() - e.min())) * (float) e.precision()) / (float) e.precision());
    }
}
