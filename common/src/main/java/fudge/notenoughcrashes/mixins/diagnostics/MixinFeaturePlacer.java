package fudge.notenoughcrashes.mixins.diagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(FeaturePlacer.class)
public class MixinFeaturePlacer {
    @WrapMethod(method = "place(Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Z)Z")
    private boolean describeFeature(PlacedFeature feature, RandomSource random, BlockPos origin, boolean biomeCheck, Operation<Boolean> original) {
        try { return original.call(feature, random, origin, biomeCheck); }
        catch (Exception failure) {
            CrashReport report = CrashReport.forThrowable(failure, "Placing world generation feature");
            var category = report.addCategory("World generation feature");
            category.setDetail("Feature", feature.feature()::toString);
            category.setDetail("Origin", origin::toString);
            category.setDetail("Placement modifiers", feature.placement()::toString);
            throw new ReportedException(report);
        }
    }
}
