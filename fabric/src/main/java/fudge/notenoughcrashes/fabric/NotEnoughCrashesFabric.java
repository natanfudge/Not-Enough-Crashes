package fudge.notenoughcrashes.fabric;

import fudge.notenoughcrashes.ModConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.api.ModInitializer;

public class NotEnoughCrashesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        NotEnoughCrashes.initialize();
    }


}
