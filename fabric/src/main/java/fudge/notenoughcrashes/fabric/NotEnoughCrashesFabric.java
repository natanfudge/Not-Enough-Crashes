package fudge.notenoughcrashes.fabric;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.fabric.config.MidnightConfig;
import fudge.notenoughcrashes.fabric.config.NecMidnightConfig;
import net.fabricmc.api.ModInitializer;

public class NotEnoughCrashesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        NotEnoughCrashes.initialize();
        MidnightConfig.init(NotEnoughCrashes.MOD_ID, NecMidnightConfig.class);
    }


}
