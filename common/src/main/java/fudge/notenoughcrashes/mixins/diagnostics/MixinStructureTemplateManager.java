package fudge.notenoughcrashes.mixins.diagnostics;
import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;
@Mixin(StructureTemplateManager.class)
public class MixinStructureTemplateManager {
    @Inject(method = "tryLoad", at = @At("RETURN"))
    private void missingStructure(Identifier id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {
        if (cir.getReturnValue().isEmpty()) NotEnoughCrashes.getLogger().warn("Missing structure template: {}", id);
    }
}
