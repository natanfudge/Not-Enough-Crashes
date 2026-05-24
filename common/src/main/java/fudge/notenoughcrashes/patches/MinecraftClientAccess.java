package fudge.notenoughcrashes.patches;

import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;

public interface MinecraftClientAccess {
     MetricsRecorder getRecorder();
     void setRecorder(MetricsRecorder recorder);
}
