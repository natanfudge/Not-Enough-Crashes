package fudge.notenoughcrashes.platform;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModsByLocation {
    private final Map<String, Set<CommonModMetadata>> locationToMod;

    private static String stringify(Set<CommonModMetadata> mods) {
        return String.format("[%s]", mods.stream().map(CommonModMetadata::name).collect(Collectors.joining(",")));
    }

    @Override
    public String toString() {
        return "Mods By Location: {\n" + locationToMod.entrySet().stream()
                .map((kv) -> "\t'" + kv.getKey() + "' -> " + stringify(kv.getValue()))
                .collect(Collectors.joining(",\n")) + "\n}";
    }

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
        String noScheme = removeAndBefore(noUnion, "//");
        // Remove '%2370!' that forge has, example:
        // union:/C:/Users/natan/.gradle/caches/fabric-loom/1.17.1/net.fabricmc.yarn.1_17_1.1.17.1+build.61-v2-forge-1.17.1-37.0.69/forge-1.17.1-37.0.69-minecraft-mapped.jar%2371!
        // We use 'removeLastPercentSymbol' instead of removing everything after last occurrence of '%' so it works with spaces as well
        // (the last space will be 'deleted', but that doesn't matter for our purposes)
        String noPercent = removeLastPercentSymbol(noScheme);
        // Remove trailing '/' and '!'
        return removeSuffix(removeSuffix(noPercent, "/"), "!");
    }

    // e.g. converts 'asdf%123fwefw' to 'asdffwefw'
    private static String removeLastPercentSymbol(String str) {
        int toRemovePos = str.lastIndexOf("%");
        if (toRemovePos == -1) return str;
        int i = toRemovePos + 1;
        for (; i < str.length(); i++) {
            // Travel until we reach a non-digit character
            if (!Character.isDigit(str.charAt(i))) break;
        }
        return str.substring(0, toRemovePos) + str.substring(i);
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