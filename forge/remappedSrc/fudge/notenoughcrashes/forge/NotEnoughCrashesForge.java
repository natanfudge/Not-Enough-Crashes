package fudge.notenoughcrashes.forge;

import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraftforge.fml.common.Mod;

@Mod(NotEnoughCrashes.MOD_ID)
public class NotEnoughCrashesForge {
    public NotEnoughCrashesForge() {
        NotEnoughCrashes.initialize();
    }
}
