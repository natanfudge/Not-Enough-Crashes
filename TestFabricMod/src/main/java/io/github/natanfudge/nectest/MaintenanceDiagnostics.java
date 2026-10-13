package io.github.natanfudge.nectest;

import com.mojang.serialization.MapCodec;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import java.util.*;

public final class MaintenanceDiagnostics {
    public static void run(MinecraftServer server) {
        var level = server.overworld();
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        server.getCommands().getDispatcher().register(Commands.literal("nec_test_command_error").executes(context -> {
            throw new IllegalStateException("NEC diagnostic command failure");
        }));
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "nec_test_command_error");
        var missing = Identifier.fromNamespaceAndPath("nec_test", "missing");
        server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, missing));
        server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, missing));
        server.getStructureTemplateManager().get(missing);
        server.getStructureTemplateManager().get(missing);
        new MobSpawnSettings.Builder().addSpawn(EntityTypes.PIG, 0, 0, 0).build();
        Feature feature = new Feature() {
            public MapCodec<? extends Feature> codec() { return null; }
            public boolean place(WorldGenLevel world, ChunkGenerator gen, RandomSource rng, BlockPos pos) {
                throw new IllegalStateException("NEC diagnostic feature failure");
            }
        };
        expect("World generation feature", "NEC diagnostic feature failure", () ->
                new PlacedFeature(Holder.direct(feature), List.of()).place(level, generator, level.getRandom(), BlockPos.ZERO));
        Structure structure = new Structure(new Structure.StructureSettings(HolderSet.empty())) {
            protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
                throw new IllegalStateException("NEC diagnostic structure failure");
            }
            public StructureType<?> type() { return null; }
        };
        expect("World generation structure", "NEC diagnostic structure failure", () ->
                structure.generate(Holder.direct(structure), level.dimension(), server.registryAccess(), generator,
                        generator.getBiomeSource(), random.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED), random, server.getStructureTemplateManager(), 0,
                        new ChunkPos(0, 0), 0, level, biome -> true));
        for (boolean atEnd : new boolean[]{false, true}) {
            StructureProcessor processor = new StructureProcessor() {
                public MapCodec<? extends StructureProcessor> codec() { return null; }
                public StructureTemplate.StructureBlockInfo processBlock(LevelReader world, BlockPos pos, BlockPos ref, BlockPos local,
                        StructureTemplate.StructureBlockInfo block, StructurePlaceSettings settings) {
                    if (!atEnd) throw new IllegalStateException("NEC diagnostic processor failure");
                    return block;
                }
                public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor world, BlockPos pos, BlockPos ref,
                        List<StructureTemplate.StructureBlockInfo> before, List<StructureTemplate.StructureBlockInfo> after, StructurePlaceSettings settings) {
                    throw new IllegalStateException("NEC diagnostic processor failure");
                }
            };
            expect("Structure processor", "NEC diagnostic processor failure", () -> StructureTemplate.processBlockInfos(level,
                    BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings().addProcessor(processor),
                    List.of(new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, Blocks.STONE.defaultBlockState(), null))));
        }
        System.out.println("NEC_TEST PASS diagnostic context: feature, structure, processor and finalizer");
    }
    private static void expect(String category, String cause, Runnable action) {
        try { action.run(); throw new AssertionError("Expected diagnostic failure: " + category); }
        catch (ReportedException failure) {
            String report = failure.getReport().getFriendlyReport(ReportType.CRASH);
            if (!report.contains("-- " + category + " --") || !cause.equals(failure.getReport().getException().getMessage())) {
                throw new AssertionError("Missing diagnostic context or original cause: " + category);
            }
        }
    }
}
