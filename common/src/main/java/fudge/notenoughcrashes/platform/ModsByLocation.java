package fudge.notenoughcrashes.platform;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModsByLocation {
    private final Map<String, Set<CommonModMetadata>> locationToMod;

    public ModsByLocation(Map<Path, Set<CommonModMetadata>> locationToMod) {
        Map<String, Set<CommonModMetadata>> mods = new HashMap<>();
        locationToMod.forEach((path, mod) -> {
            mods.put(normalizeString(path.toUri().toString()), mod);
        });
        this.locationToMod = mods;
    }

    public Set<CommonModMetadata> get(URI path) {
        return locationToMod.get(normalizeString(path.toString()));
    }

    public Set<CommonModMetadata> get(Path path) {
        return get(path.toUri());
    }

    private static String removeSuffix(String str, String suffix) {
        return str.endsWith(suffix) ? str.substring(0, str.length() - suffix.length()) : str;
    }

    private static String normalizeString(String path) {
        int schemeMarker = path.lastIndexOf("//");
        // Remove scheme marker
        String noScheme = schemeMarker == -1 ? path : path.substring(schemeMarker + 2);
        // Remove trailing '/' and '!'
        return removeSuffix(removeSuffix(noScheme, "/"), "!");
    }

}