package fudge.notenoughcrashes.mixins.diagnostics;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(Commands.class)
public class MixinCommands {
    @ModifyExpressionValue(method = "performCommand", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z"))
    private boolean logCommandExceptions(boolean enabled) { return true; }
}
