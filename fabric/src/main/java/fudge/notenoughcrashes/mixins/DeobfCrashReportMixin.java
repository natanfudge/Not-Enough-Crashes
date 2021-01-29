package fudge.notenoughcrashes.mixins;

import fudge.notenoughcrashes.patches.PatchedCrashReport;
import fudge.notenoughcrashes.fabric.StacktraceDeobfuscator;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO: move all fields that should be in common

@Mixin(value = CrashReport.class, priority = 500)
public abstract class DeobfCrashReportMixin implements PatchedCrashReport {
    @Shadow
    @Final
    private Throwable cause;
    /**
     * @reason Deobfuscate the stacktrace
     */
    @Inject(method = "fillSystemDetails", at = @At("HEAD"))
    private void beforeFillSystemDetails(CallbackInfo ci) {
        StacktraceDeobfuscator.deobfuscateThrowable(cause);
    }

}
