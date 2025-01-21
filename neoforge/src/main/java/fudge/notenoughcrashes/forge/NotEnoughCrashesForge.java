package fudge.notenoughcrashes.forge;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.MidnightConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;


@Mod(NotEnoughCrashes.MOD_ID)
public class NotEnoughCrashesForge {
    public NotEnoughCrashesForge() {
        NotEnoughCrashes.initialize();
    }
}
