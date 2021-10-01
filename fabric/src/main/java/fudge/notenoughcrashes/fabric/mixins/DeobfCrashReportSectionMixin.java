package fudge.notenoughcrashes.fabric.mixins;

import fudge.notenoughcrashes.fabric.StacktraceDeobfuscator;
import fudge.notenoughcrashes.patches.PatchedCrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CrashReportSection.class)
public class DeobfCrashReportSectionMixin {

    @Shadow private StackTraceElement[] stackTrace;

    /**
     * @reason Disable stack trace pruning
     */
    @Overwrite
    public void trimStackTraceEnd(int size) {
        stackTrace = StacktraceDeobfuscator.deobfuscateStacktrace(Thread.currentThread().getStackTrace(),true);
    }
}
