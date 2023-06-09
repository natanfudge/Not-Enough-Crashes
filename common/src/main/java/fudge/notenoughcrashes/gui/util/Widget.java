package fudge.notenoughcrashes.gui.util;

import net.minecraft.client.gui.DrawContext;

public interface Widget {
    void draw(DrawContext context);

    void onClick(double clickX, double clickY);
}
