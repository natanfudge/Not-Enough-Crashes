package fudge.notenoughcrashes.fabric.mixins;

import fudge.notenoughcrashes.fabric.StacktraceDeobfuscator;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrashReport.class, priority = 500)
public abstract class DeobfCrashReportMixin {
    @Shadow
    @Final
    private Throwable cause;
    /**
     * @reason Deobfuscate the stacktrace
     */
    @Inject(method = "addStackTrace", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SystemDetails;writeTo(Ljava/lang/StringBuilder;)V"))
    private void beforeWritingSystemDetails(CallbackInfo ci) {
        StacktraceDeobfuscator.deobfuscateThrowable(cause);
    }

}
