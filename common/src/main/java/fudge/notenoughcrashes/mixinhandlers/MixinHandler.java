package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.Level;

public class MixinHandler {
    public static void placeBlameOnBrokenStructures(Biome biome, ChunkRegion chunkRegion, StructureFeature<?> structureFeature, CrashReport crashreport) {
        DynamicRegistryManager dynamicRegistries = chunkRegion.getRegistryManager();

        String structureName = structureFeature.getName();
        Identifier structureID = dynamicRegistries.get(Registry.STRUCTURE_FEATURE_KEY).getId(structureFeature);
        Identifier biomeID = dynamicRegistries.get(Registry.BIOME_KEY).getId(biome);

        // Add extra info to the crash report file.
        // Note, only structures can do the details part as configuredfeatures always says the ConfiguredFeature class.
        crashreport.getSystemDetailsSection()
                .addSection("\n****************** Blame Report ******************",
                        "\n\n Structure Name : " + (structureName != null ? structureName : "Someone set the structure's name to null which is... very bad.") +
                                "\n Structure Registry Name : " + (structureID != null ? structureID : "Structure is not registered somehow. Yell at the mod author when found to register their structures!") +
                                "\n Structure Details : " + structureFeature.toString() +
                                "\n Biome Registry Name : " + (biomeID != null ? biomeID : "Wait what? How is the biome not registered and has no registry name!?!? This should be impossible!!!"));

        // Log it to the latest.log file as well.
        NotEnoughCrashes.LOGGER.log(Level.ERROR, crashreport.getMessage());
    }
}
