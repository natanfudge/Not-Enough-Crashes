package fudge.notenoughcrashes.mixins.client;

import com.mojang.renderpearl.frontend.FrontendGpuSurface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FrontendGpuSurface.class)
public interface GpuSurfaceAccessor {
    @Accessor("hasBlittedTexture") boolean nec$hasBlittedTexture();
}
