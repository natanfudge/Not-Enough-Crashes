package fudge.notenoughcrashes.forge;

import dev.architectury.platform.forge.EventBuses;
import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NotEnoughCrashes.MOD_ID)
public class NotEnoughCrashesForge {
    public NotEnoughCrashesForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(NotEnoughCrashes.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        NotEnoughCrashes.initialize();
    }
}
