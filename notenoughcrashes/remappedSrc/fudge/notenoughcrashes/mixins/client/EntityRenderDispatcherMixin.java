package fudge.notenoughcrashes.mixins.client;

import java.util.Iterator;

import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.entity.EntityRenderDispatcher;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /**
     * This is to prevent the exception at the end of the method from going off in case something went wrong with the entity type registry.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private boolean redirectHasNext(Iterator iterator) {
        if (EntryPointCatcher.crashedDuringStartup()) {
            return false;
        } else {
            return iterator.hasNext();
        }
    }
}
