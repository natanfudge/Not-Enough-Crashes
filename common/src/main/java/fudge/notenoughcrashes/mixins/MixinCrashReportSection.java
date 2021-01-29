package fudge.notenoughcrashes.mixins;

import fudge.notenoughcrashes.patches.PatchedCrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CrashReportSection.class)
public class MixinCrashReportSection {
//TODO: maybe restore this and CrashReportSectionElementMixin if the formatting is truely bad, and the refmap issue gets fixed.

//    @Shadow @Final private String title;
//    @Shadow @Final private List<?> elements;

//    /**
//     * @reason Improve crash report formatting
//     **/
//    @Overwrite
//    public void addStackTrace(StringBuilder builder) {
//        builder.append("-- ").append(title).append(" --\n");
//        for (Object elementObject : elements) {
//            PatchedCrashReport.Element element = (PatchedCrashReport.Element) elementObject;
//
//            String sectionIndent = "  ";
//
//            builder.append(sectionIndent)
//                    .append(element.invokeGetName())
//                    .append(": ");
//
//            StringBuilder indent = new StringBuilder(sectionIndent + "  ");
//            for (char ignored : element.invokeGetName().toCharArray()) {
//                indent.append(" ");
//            }
//
//            boolean first = true;
//            for (String line : element.invokeGetDetail().trim().split("\n")) {
//                if (!first) {
//                    builder.append("\n").append(indent);
//                }
//                first = false;
//                if (line.startsWith("\t")) {
//                    line = line.substring(1);
//                }
//                builder.append(line.replace("\t", ""));
//            }
//
//            builder.append("\n");
//        }
//    }
}
