package fudge.notenoughcrashes.mixins;

import fudge.notenoughcrashes.patches.PatchedCrashReport;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
@Mixin(value = CrashReport.class, priority = 500)
public abstract class MixinCrashReport implements PatchedCrashReport {

    @Shadow
    @Final
    private SystemDetails systemDetailsSection;
    @Shadow
    @Final
    private List<CrashReportSection> otherSections;
    @Shadow
    @Final
    private Throwable cause;
    @Shadow
    @Final
    private String message;


    private Set<CommonModMetadata> suspectedMods;

    // We inject into the constructor so we can have access to the Throwable. Otherwise [cause] will be null.
    @Inject(method = "<init>", at = @At("TAIL"))
    private void atConstruction(String message, Throwable cause, CallbackInfo ci) {
        suspectedMods = ModIdentifier.identifyFromStacktrace(cause);
    }

    @Shadow
    private static String generateWittyComment() {
        return null;
    }

    private static String stacktraceToString(Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public Set<CommonModMetadata> getSuspectedMods() {
        return suspectedMods;
    }


    /**
     * @reason Adds a list of mods which may have caused the crash to the report.
     */
    @Inject(method = "addStackTrace", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SystemDetails;writeTo(Ljava/lang/StringBuilder;)V"))
    private void beforeSystemDetailsAreWritten(CallbackInfo ci) {
        systemDetailsSection.addSection("Suspected Mods", () -> {
            try {
                if (!suspectedMods.isEmpty()) {
                    return suspectedMods.stream()
                            .map((mod) -> mod.getName() + " (" + mod.getId() + ")")
                            .collect(Collectors.joining(", "));
                } else return "None";
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }
}
