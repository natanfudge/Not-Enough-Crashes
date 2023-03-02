package fudge.notenoughcrashes.forge;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.MidnightConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(NotEnoughCrashes.MOD_ID)
public class NotEnoughCrashesForge {
    public NotEnoughCrashesForge() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, screen) -> MidnightConfig.getScreen(screen, NotEnoughCrashes.MOD_ID)
                )
        );
        NotEnoughCrashes.initialize();
    }
}
