package fudge.notenoughcrashes.mixins;

import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.crash.CrashReport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Collectors;


@Mixin(value = CrashReport.class, priority = 500)
public abstract class MixinCrashReport {

    @Shadow
    @Final
    private SystemDetails systemDetailsSection;

    private CrashReport getThis() {
        return (CrashReport) (Object) this;
    }

    /**
     * @reason Adds a list of mods which may have caused the crash to the report.
     */
    @Inject(method = "addStackTrace", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SystemDetails;writeTo(Ljava/lang/StringBuilder;)V"))
    private void beforeSystemDetailsAreWritten(CallbackInfo ci) {
        systemDetailsSection.addSection("Suspected Mods", () -> {
            try {
                var suspectedMods = ModIdentifier.getSuspectedModsOf(getThis());
                if (!suspectedMods.isEmpty()) {
                    return suspectedMods.stream()
                            .map((mod) -> mod.name() + " (" + mod.id() + ")")
                            .collect(Collectors.joining(", "));
                } else return "None";
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }
}
