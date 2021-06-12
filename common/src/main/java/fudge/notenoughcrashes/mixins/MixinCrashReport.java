package fudge.notenoughcrashes.mixins;

import fudge.notenoughcrashes.patches.PatchedCrashReport;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

//TODO: move all fields that should be in common

@SuppressWarnings("unused")
@Mixin(value = CrashReport.class, priority = 500)
public abstract class MixinCrashReport implements PatchedCrashReport {

    private static final boolean ANNOYING_EASTER_EGG_DISABLED = true;
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
    private Set<CommonModMetadata> suspectedMods = null;

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
    @Inject(method = "fillSystemDetails", at = @At("TAIL"))
    private void afterFillSystemDetails(CallbackInfo ci) {
        systemDetailsSection.addSection("Suspected Mods", () -> {
            try {
                suspectedMods = ModIdentifier.identifyFromStacktrace(cause);

                List<String> modNames = new ArrayList<>();
                for (CommonModMetadata mod : suspectedMods) {
                    modNames.add(mod.getName() + " (" + mod.getId() + ")");
                }

                if (!modNames.isEmpty()) return StringUtils.join(modNames, ", ");
                else return "Unknown";
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }


    //TODO: unlikely that this is an improvement anymore
//    /**
//     * @reason Improve report formatting
//     */
//    @Overwrite
//    public void addStackTrace(StringBuilder builder) {
//        for (CrashReportSection section : otherSections) {
//            section.addStackTrace(builder);
//            builder.append("\n");
//        }
//
//        systemDetailsSection.writeTo(builder);
//    }

    private String generateEasterEggComment() {
        try {
            String comment = generateWittyComment();

            if (comment.contains("Dinnerbone")) {
                CommonModMetadata mod = suspectedMods.iterator().next();
                if (!mod.getAuthors().isEmpty()) {
                    String author = mod.getAuthors().iterator().next();
                    comment = comment.replace("Dinnerbone", author);
                }
            }

            return comment;
        } catch (Throwable ignored) {
        }

        return generateWittyComment();
    }
}
