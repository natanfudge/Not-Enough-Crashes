package fudge.notenoughcrashes.mixins;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntity.class, priority = 10000)
public class MixinTileEntity {

    private boolean noNBT = false;

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "populateCrashReport", at = @At("TAIL"))
    private void onPopulateCrashReport(CrashReportCategory section, CallbackInfo ci) {
        if (!noNBT) {
            noNBT = true;
            var self = (BlockEntity) (Object) this;
            var world = self.getLevel();
            if (world != null) {
                section.setDetail("Block Entity NBT", () -> self.saveCustomOnly(world.registryAccess()).toString());
            }
            noNBT = false;
        }
    }
}
