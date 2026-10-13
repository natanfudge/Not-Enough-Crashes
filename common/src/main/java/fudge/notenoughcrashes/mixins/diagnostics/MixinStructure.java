package fudge.notenoughcrashes.mixins.diagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import java.util.function.Predicate;
@Mixin(Structure.class)
public class MixinStructure {
    @WrapMethod(method = "generate")
    private StructureStart describeStructure(Holder<Structure> selected, ResourceKey<Level> dimension, RegistryAccess registries,
            ChunkGenerator generator, BiomeSource biomes, Climate.Sampler climate, RandomState random,
            StructureTemplateManager templates, long seed, ChunkPos chunk, int references, LevelHeightAccessor height,
            Predicate<Holder<Biome>> validBiome, Operation<StructureStart> original) {
        try { return original.call(selected, dimension, registries, generator, biomes, climate, random, templates, seed, chunk, references, height, validBiome); }
        catch (Exception failure) {
            CrashReport report = CrashReport.forThrowable(failure, "Generating structure");
            var category = report.addCategory("World generation structure");
            category.setDetail("Structure", selected::toString);
            category.setDetail("Chunk", chunk::toString);
            category.setDetail("Dimension", dimension::toString);
            throw new ReportedException(report);
        }
    }
}
