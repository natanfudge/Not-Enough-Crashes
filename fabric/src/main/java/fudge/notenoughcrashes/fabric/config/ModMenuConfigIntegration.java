package fudge.notenoughcrashes.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.MidnightConfig;

public class ModMenuConfigIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
         return parent -> MidnightConfig.getScreen(parent, NotEnoughCrashes.MOD_ID);
    }
}
