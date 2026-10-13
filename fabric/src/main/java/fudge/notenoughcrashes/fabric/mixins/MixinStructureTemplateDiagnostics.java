package fudge.notenoughcrashes.fabric.mixins;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;
@Mixin(StructureTemplate.class)
public class MixinStructureTemplateDiagnostics {
    private static ReportedException report(Exception failure, StructureProcessor processor, BlockPos position) {
        CrashReport report = CrashReport.forThrowable(failure, "Processing structure blocks");
        var category = report.addCategory("Structure processor");
        category.setDetail("Processor", processor.getClass().getName());
        category.setDetail("Position", position::toString);
        return new ReportedException(report);
    }
    @WrapOperation(method = "processBlockInfos(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;)Ljava/util/List;", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessor;processBlock(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;"))
    private static StructureTemplate.StructureBlockInfo describeProcessor(StructureProcessor processor, LevelReader level, BlockPos position,
            BlockPos reference, BlockPos local, StructureTemplate.StructureBlockInfo block, StructurePlaceSettings settings,
            Operation<StructureTemplate.StructureBlockInfo> original) {
        try { return original.call(processor, level, position, reference, local, block, settings); }
        catch (Exception failure) { throw report(failure, processor, position); }
    }
    @WrapOperation(method = "processBlockInfos(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessor;finalizeProcessing(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Ljava/util/List;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;)Ljava/util/List;"))
    private static List<StructureTemplate.StructureBlockInfo> describeFinalizer(StructureProcessor processor, net.minecraft.world.level.ServerLevelAccessor level, BlockPos position,
            BlockPos reference, List<StructureTemplate.StructureBlockInfo> before, List<StructureTemplate.StructureBlockInfo> after,
            StructurePlaceSettings settings, Operation<List<StructureTemplate.StructureBlockInfo>> original) {
        try { return original.call(processor, level, position, reference, before, after, settings); }
        catch (Exception failure) { throw report(failure, processor, position); }
    }
}
