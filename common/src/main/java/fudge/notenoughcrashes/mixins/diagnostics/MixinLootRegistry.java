package fudge.notenoughcrashes.mixins.diagnostics;
import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Mixin(ReloadableServerRegistries.Holder.class)
public class MixinLootRegistry {
    @Shadow @Final private HolderLookup.Provider registries;
    @Unique private final Set<ResourceKey<LootTable>> nec$missing = ConcurrentHashMap.newKeySet();
    @Inject(method = "getLootTable", at = @At("RETURN"))
    private void missingLoot(ResourceKey<LootTable> id, CallbackInfoReturnable<LootTable> cir) {
        if (cir.getReturnValue() == LootTable.EMPTY && registries.lookup(Registries.LOOT_TABLE).flatMap(r -> r.get(id)).isEmpty()
                && nec$missing.add(id)) NotEnoughCrashes.getLogger().warn("Missing loot table: {}", id.identifier());
    }
}
