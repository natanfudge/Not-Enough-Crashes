package fudge.notenoughcrashes.mixins.diagnostics;
import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;
@Mixin(MobSpawnSettings.class)
public class MixinMobSpawnSettings {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void validateSpawns(Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawns,
                               Map<?, ?> costs, CallbackInfo ci) {
        spawns.forEach((category, entries) -> entries.unwrap().forEach(entry -> {
            var spawn = entry.value();
            int min = spawn.count().minInclusive(), max = spawn.count().maxInclusive();
            if (entry.weight() <= 0 || min <= 0 || max < min) {
                NotEnoughCrashes.getLogger().warn("Invalid biome spawn: entity={}, category={}, weight={}, min={}, max={}",
                        BuiltInRegistries.ENTITY_TYPE.getKey(spawn.type()), category, entry.weight(), min, max);
            }
        }));
    }
}
