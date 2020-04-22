package fudge.notenoughcrashes.gui.util;

import net.minecraft.client.util.math.MatrixStack;

public interface Widget {
    void draw(MatrixStack stack);

    void onClick(double clickX, double clickY);
}
