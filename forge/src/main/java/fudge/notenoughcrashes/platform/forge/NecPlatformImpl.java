package fudge.notenoughcrashes.platform.forge;

import fudge.notenoughcrashes.mixins.client.SplashScreenMixin;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class NecPlatformImpl implements NecPlatform {

    @Override
    public  void setSplashScreenLogo(Identifier newLogo) {
        SplashScreenMixin.setLogo(newLogo);
    }

    @Override
    public  Map<URI, Set<CommonModMetadata>> getModsAtLocationsInDisk() {
        Map<URI, Set<CommonModMetadata>> modMap = new HashMap<>();

        for (ModFileInfo modFile : ModList.get().getModFiles()) {
            URI modJar = modFile.getFile().getFilePath().toUri();
            for (IModInfo modInfo : modFile.getMods()) {
                if (!(modInfo instanceof ModInfo)) continue;
                modMap.computeIfAbsent(modJar, f -> new HashSet<>()).add(toCommon((ModInfo) modInfo));
            }
        }

        return modMap;
    }

    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }


    private static CommonModMetadata toCommon(ModInfo mod) {
        URL issueUrl = mod.getOwningFile().getIssueURL();
        String authors =  (String)mod.getConfigElement("authors").orElse(null);
        //TODO: make sure the issues page thing works
        return new CommonModMetadata(mod.getModId(), mod.getDisplayName(), issueUrl == null ? null : issueUrl.toExternalForm(),
               authors == null ? null : Collections.singletonList(authors));
    }
}
