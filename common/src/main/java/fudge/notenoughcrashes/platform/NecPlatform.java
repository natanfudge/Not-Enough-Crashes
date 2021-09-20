package fudge.notenoughcrashes.platform;

import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NecPlatform {
    static NecPlatform instance() {
        return NecPlatformStorage.INSTANCE_SET_ONLY_BY_SPECIFIC_PLATFORMS_VERY_EARLY;
    }


    ModsByLocation getModsAtLocationsInDisk();

    Path getGameDirectory();

    Path getConfigDirectory();

    boolean isDevelopmentEnvironment();

    /**
     * Get be multiple metadatas because forge supports having multiple mods under one jar
     */
    List<CommonModMetadata> getModMetadatas(String modId);

}
