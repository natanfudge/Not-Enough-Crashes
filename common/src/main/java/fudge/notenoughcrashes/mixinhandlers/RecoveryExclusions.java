package fudge.notenoughcrashes.mixinhandlers;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.NecConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Crashes matching an entry of the {@code recoveryExclusions} config are left to vanilla instead of being recovered.
 * An entry is {@code fully.qualified.ExceptionClass} or {@code fully.qualified.ExceptionClass:message text}.
 */
public class RecoveryExclusions {
    private static final Logger LOGGER = LogManager.getLogger(NotEnoughCrashes.NAME + " Recovery Exclusions");

    public static boolean isExcluded(Throwable error) {
        String entry = findMatch(error, NecConfig.getCurrent().recoveryExclusions());
        if (entry == null) return false;
        LOGGER.info("Not recovering from {} because it matches recoveryExclusions entry '{}'", error, entry);
        return true;
    }

    public static boolean matches(Throwable error, List<String> entries) {
        return findMatch(error, entries) != null;
    }

    private static String findMatch(Throwable error, List<String> entries) {
        if (entries == null || entries.isEmpty()) return null;
        // Suppressed exceptions are not checked: they are usually side effects of the actual failure.
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = error; current != null && seen.add(current); current = current.getCause()) {
            for (String entry : entries) {
                if (entryMatches(current, entry)) return entry;
            }
        }
        return null;
    }

    private static boolean entryMatches(Throwable error, String entry) {
        if (entry == null || entry.isBlank()) return false;
        // Class names can't contain ':', so the first one separates the class from the message text.
        int separator = entry.indexOf(':');
        String className = (separator == -1 ? entry : entry.substring(0, separator)).trim();
        if (className.isEmpty()) {
            LOGGER.warn("Ignoring recoveryExclusions entry '{}' because it has no exception class", entry);
            return false;
        }
        if (!error.getClass().getName().equals(className)) return false;
        if (separator == -1) return true;
        String message = error.getMessage();
        return message != null && message.contains(entry.substring(separator + 1));
    }
}
