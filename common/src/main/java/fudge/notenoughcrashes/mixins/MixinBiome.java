/*
MIT License
Copyright 2020 TelepathicGrunt
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fudge.notenoughcrashes.mixins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/* @author - TelepathicGrunt
 *
 * Two small mixins to make crashes during feature gen and structure gen now
 * output info about the feature, structure, and biome into the crashlog and
 * into the latest.log. Basically it needs more info as it is impossible
 * to find the broken feature before.
 *
 * I give permission to relicense this code under MIT and for you to do
 * whatever you want lol. Hopefully this will tae care of the licensing
 * stuff as the original code in my Blame mod is LGPLv3
 */
@Mixin(Biome.class)
public class MixinBiome {
	// currently removing because so many parameters is unmaintainable.
//	/**
//	 * Place blame on broke feature during worldgen.
//	 * Prints registry name of feature and biome.
//	 * Prints the crashlog to latest.log as well.
//	 *
//	 * I tried using crashreport.addElement( ) to add
//	 * a new section in the crashreport but it crashes
//	 * the code instead and I have zero clue why.
//	 * If you can figure out why, I would love to know!
//	 * Otherwise, I just tack on the extra info at end
//	 * of the crash report.
//	 */
//	@Inject(method = "generateFeatureStep(Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/ChunkRegion;JLnet/minecraft/world/gen/ChunkRandom;Lnet/minecraft/util/math/BlockPos;)V",
//			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;", ordinal = 1),
//			locals = LocalCapture.CAPTURE_FAILHARD)
//	private void addFeatureDetails(StructureAccessor structureAccessor, ChunkGenerator chunkGenerator,
//								   ChunkRegion chunkRegion, long seed, ChunkRandom random, BlockPos pos,
//								   CallbackInfo ci, List<List<Supplier<ConfiguredFeature<?, ?>>>> GenerationStageList,
//								   int numOfGenerationStage, int generationStageIndex, int configuredFeatureIndex,
//								   Iterator<ConfiguredFeature<?, ?>> var12, Supplier<ConfiguredFeature<?, ?>> supplier, ConfiguredFeature<?, ?> configuredfeature,
//								   Exception exception, CrashReport crashreport)
//	{
//		DynamicRegistryManager dynamicRegistries = chunkRegion.getRegistryManager();
//		Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Make JSON form pretty for crashing ConfiguredFeatures
//
//		Identifier configuredFeatureID = dynamicRegistries.get(Registry.CONFIGURED_FEATURE_KEY).getId(configuredfeature);
//		Identifier biomeID = dynamicRegistries.get(Registry.BIOME_KEY).getId((Biome)(Object)this);
//		Optional<JsonElement> configuredFeatureJSON = ConfiguredFeature.CODEC.encode(configuredfeature, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
//
//		// Add extra info to the crash report file.
//		crashreport.getSystemDetailsSection()
//				.addSection("\n****************** Not Enough Crashes Report ******************",
//				"\n\n ConfiguredFeature Registry Name : " + (configuredFeatureID != null ? configuredFeatureID : "Has no identifier as it was not registered... go yell at the mod owner when you find them! lol") +
//					"\n Biome Registry Name : " + (biomeID != null ? biomeID : "Wait what? How is the biome not registered and has no registry name!?!? This should be impossible!!!") +
//					"\n\n JSON info : " + (configuredFeatureJSON.isPresent() ? gson.toJson(configuredFeatureJSON.get()) : "Failed to generate JSON somehow.") + "\n\n");
//
//		// Log it to the latest.log file as well.
//		NotEnoughCrashes.LOGGER.log(Level.ERROR, crashreport.getMessage());
//	}

}