package org.dimdev.toomanycrashes.mixins;

import org.dimdev.toomanycrashes.PatchedCrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.util.crash.CrashReportSection$Element")
public abstract class CrashReportSectionElementMixin implements PatchedCrashReport.Element {

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract String getDetail();

    @Override
    public String invokeGetName() {
        return getName();
    }

    @Override
    public String invokeGetDetail() {
        return getDetail();
    }
}
