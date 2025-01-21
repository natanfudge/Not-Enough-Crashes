package fudge.notenoughcrashes.forge.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.MidnightConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = NotEnoughCrashes.MOD_ID, dist = Dist.CLIENT)
public class NotEnoughCrashesForgeClient {
    public NotEnoughCrashesForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, screen) -> MidnightConfig.getScreen(screen, NotEnoughCrashes.MOD_ID)
        );

        NotEnoughCrashes.initialize();
    }
}
