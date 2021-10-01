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
            mods.put(normalizePathString(path.toUri().toString()), mod);
        });
        this.locationToMod = mods;
    }

    public Set<CommonModMetadata> get(URI path) {
        return locationToMod.get(normalizePathString(path.toString()));
    }

    public Set<CommonModMetadata> get(Path path) {
        return get(path.toUri());
    }



    private static String normalizePathString(String path) {
        // Remove 'union:/' that forge has
        String noUnion = removePrefix(path, "union:/");
        // Remove scheme marker
        String noScheme = removeAndBefore(noUnion,"//");
        // Remove '%2370!' that forge has
        String noPercent = removeAndAfter(noScheme,"%");
        // Remove trailing '/' and '!'
        return removeSuffix(removeSuffix(noPercent, "/"), "!");
    }


    private static String removeSuffix(String str, String suffix) {
        return str.endsWith(suffix) ? str.substring(0, str.length() - suffix.length()) : str;
    }

    private static String removePrefix(String str, String suffix) {
        return str.startsWith(suffix) ? str.substring(suffix.length()) : str;
    }

    private static String removeAndBefore(String str, String toRemove) {
        int toRemovePos = str.lastIndexOf(toRemove);
        if (toRemovePos == -1) return str;
        else return str.substring(toRemovePos + toRemove.length());
    }
    private static String removeAndAfter(String str, String toRemove) {
        int toRemovePos = str.lastIndexOf(toRemove);
        if (toRemovePos == -1) return str;
        else return str.substring(0, toRemovePos);
    }
}