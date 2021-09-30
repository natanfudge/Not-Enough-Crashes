/*
MIT License
Copyright 2020 TelepathicGrunt
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fudge.notenoughcrashes.forge.mixins;

import fudge.notenoughcrashes.mixinhandlers.MixinHandler;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

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
public class MixinBiomeStructureDetails {

    /**
     * Place blame on broke structures during worldgen.
     * Prints registry name of feature and biome.
     * Prints the crashlog to latest.log as well.
     */
    @Inject(method = "generateFeatureStep(Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/ChunkRegion;JLnet/minecraft/world/gen/ChunkRandom;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void addStructureDetails(StructureAccessor structureManager, ChunkGenerator chunkGenerator, ChunkRegion chunkRegion,
                                     long seed, ChunkRandom rand, BlockPos pos, CallbackInfo ci, List<?> list, int i, int j, int k, Iterator<?>
                                             var12, StructureFeature<?> structureFeature, int l, int i1, int j1, int k1, Exception exception, CrashReport crashreport) {
        MixinHandler.placeBlameOnBrokenStructures((Biome) (Object) this, chunkRegion, structureFeature, crashreport);
    }
}