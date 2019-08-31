package org.dimdev.toomanycrashes.mixins;

import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dimdev.toomanycrashes.PatchedCrashReport;
import org.dimdev.toomanycrashes.StacktraceDeobfuscator;
import org.dimdev.utils.ModIdentifier;
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

@Mixin(value = CrashReport.class, priority = 500)
public abstract class MixinCrashReport implements PatchedCrashReport {

    private static final boolean ANNOYING_EASTER_EGG_DISABLED = true;
    @Shadow @Final private CrashReportSection systemDetailsSection;
    @Shadow @Final private List<CrashReportSection> otherSections;
    @Shadow @Final private Throwable cause;
    @Shadow @Final private String message;
    private Set<ModMetadata> suspectedMods = null;

    @Shadow private static String generateWittyComment() {
        return null;
    }

    private static String stacktraceToString(Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public Set<ModMetadata> getSuspectedMods() {
        return suspectedMods;
    }

    /**
     * @reason Deobfuscate the stacktrace
     */
    @Inject(method = "fillSystemDetails", at = @At("HEAD"))
    private void beforeFillSystemDetails(CallbackInfo ci) {
        StacktraceDeobfuscator.deobfuscateThrowable(cause);
    }

    /**
     * @reason Adds a list of mods which may have caused the crash to the report.
     */
    @Inject(method = "fillSystemDetails", at = @At("TAIL"))
    private void afterFillSystemDetails(CallbackInfo ci) {
        systemDetailsSection.add("Suspected Mods", () -> {
            try {
                suspectedMods = ModIdentifier.identifyFromStacktrace(cause);

                String modListString = "Unknown";
                List<String> modNames = new ArrayList<>();
                for (ModMetadata mod : suspectedMods) {
                    modNames.add(mod.getName() + " (" + mod.getId() + ")");
                }

                if (!modNames.isEmpty()) {
                    modListString = StringUtils.join(modNames, ", ");
                }
                return modListString;
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }

    /**
     * @reason Improve report formatting
     */
    @Overwrite
    public String asString() {
        StringBuilder builder = new StringBuilder();

        builder.append("---- Minecraft Crash Report ----\n")
                .append("// ").append(ANNOYING_EASTER_EGG_DISABLED ? generateWittyComment() : generateEasterEggComment())
                .append("\n\n")
                .append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date())).append("\n")
                .append("Description: ").append(message)
                .append("\n\n")
                .append(stacktraceToString(cause)
                        .replace("\t", "    ")) // Vanilla's getCauseStackTraceOrString doesn't print causes and suppressed exceptions
                .append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; i++) {
            builder.append("-");
        }

        builder.append("\n\n");
        addStackTrace(builder);
        return builder.toString().replace("\t", "    ");
    }

    /**
     * @reason Improve report formatting
     */
    @Overwrite
    public void addStackTrace(StringBuilder builder) {
        for (CrashReportSection section : otherSections) {
            section.addStackTrace(builder);
            builder.append("\n");
        }

        systemDetailsSection.addStackTrace(builder);
    }

    private String generateEasterEggComment() {
        try {
            String comment = generateWittyComment();

            if (comment.contains("Dinnerbone")) {
                ModMetadata mod = suspectedMods.iterator().next();
                if (!mod.getAuthors().isEmpty()) {
                    String author = mod.getAuthors().iterator().next().getName();
                    comment = comment.replace("Dinnerbone", author);
                }
            }

            return comment;
        } catch (Throwable ignored) {
        }

        return generateWittyComment();
    }
}
