package io.github.natanfudge.nectest.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface MaintenanceLevelAccessor {
    @Mutable
    @Accessor("neighborUpdater")
    void necTest$setNeighborUpdater(CollectingNeighborUpdater updater);
}
