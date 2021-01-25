package fudge.notenoughcrashes.platform;

import net.minecraft.util.Identifier;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface NecPlatform {
    static NecPlatform instance() {
        return NecPlatformStorage.INSTANCE_SET_ONLY_BY_SPECIFIC_PLATFORMS_VERY_EARLY;
    }

    void setSplashScreenLogo(Identifier newLogo);

    Map<URI, Set<CommonModMetadata>> getModsAtLocationsInDisk();

    Path getGameDirectory();

    Path getConfigDirectory();

    boolean isDevelopmentEnvironment();



}
