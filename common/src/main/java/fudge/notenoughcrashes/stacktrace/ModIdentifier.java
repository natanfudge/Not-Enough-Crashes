package fudge.notenoughcrashes.stacktrace;

import fudge.notenoughcrashes.NecConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.ModsByLocation;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModIdentifier {

    private static final Map<CrashReport, Set<CommonModMetadata>> suspectedModsCache = new HashMap<>();

    public static Set<CommonModMetadata> getSuspectedModsOf(CrashReport report) {
        return suspectedModsCache.computeIfAbsent(report, (ignored) -> identifyFromStacktrace(report.getCause()));
    }

    @NotNull
    private static Set<CommonModMetadata> identifyFromStacktrace(Throwable e) {
        Set<CommonModMetadata> mods = new HashSet<>();
        // Include suppressed exceptions too
        visitChildrenThrowables(e, throwable -> {
            for (CommonModMetadata newMod : identifyFromThrowable(throwable)) {
                if (mods.stream().noneMatch(mod -> mod.id().equals(newMod.id()))) {
                    mods.add(newMod);
                }
            }
        });
        return mods;
    }

    private static void visitChildrenThrowables(Throwable e, Consumer<Throwable> visitor) {
        visitor.accept(e);
        for (Throwable child : e.getSuppressed()) visitChildrenThrowables(child, visitor);
    }

    private static Set<CommonModMetadata> identifyFromThrowable(Throwable e) {
        ModsByLocation modMap = NecPlatform.instance().getModsAtLocationsInDisk();

        Set<String> involvedClasses = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                involvedClasses.add(element.getClassName());
            }
            e = e.getCause();
        }

        Set<CommonModMetadata> mods = new LinkedHashSet<>();
        for (String className : involvedClasses) {
            Set<CommonModMetadata> classMods = identifyFromClass(className, modMap);
            mods.addAll(classMods);
        }
        debug(modMap::toString);
        return mods;
    }

    private static final boolean FORCE_DEBUG = false;

    private static void debug(Supplier<String> message) {
        if (FORCE_DEBUG || NecConfig.instance().debugModIdentification) NotEnoughCrashes.getLogger().info(message.get());
    }

    // TODO: get a list of mixin transformers that affected the class and blame those too
    @NotNull
    private static Set<CommonModMetadata> identifyFromClass(String className, ModsByLocation modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            debug(() -> "Ignoring class " + className + " for identification because it is a mixin class");
            return Collections.emptySet();
        }

        try {
            // Get the URL of the class
            Class<?> clazz = Class.forName(className);
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                debug(() -> "Ignoring class " + className + " for identification because the code source could not be found");
                return Collections.emptySet(); // Some internal native sun classes
            }
            URL url = codeSource.getLocation();

            if (url == null) {
                NotEnoughCrashes.getLogger().warn("Failed to identify mod for " + className);
                return Collections.emptySet();
            }

            // Get the mod containing that class
            Set<CommonModMetadata> mods = getModsAt(Paths.get(url.toURI()), modMap);
            if (NecConfig.instance().debugModIdentification && !mods.isEmpty()){
                debug(() -> "Successfully placed blame of '" + className + "' on '"
                        + mods.stream().findFirst().get().name() + "'");
            }
            return mods;
        } catch (URISyntaxException | ClassNotFoundException | NoClassDefFoundError | FileSystemNotFoundException e) {
            debug(() -> "Ignoring class " + className + " for identification because an error occurred");
            if (NecConfig.instance().debugModIdentification) {
                e.printStackTrace();
            }
            return Collections.emptySet(); // we cannot do it
        }
    }

    @NotNull
    private static Set<CommonModMetadata> getModsAt(Path path, ModsByLocation modMap) {
        Set<CommonModMetadata> mod = modMap.get(path);
        if (mod != null) return mod;
        else if (NecPlatform.instance().isDevelopmentEnvironment()) {

            // For some reason, in dev, the mod being tested has the 'resources' folder as the origin instead of the 'classes' folder.
            String resourcesPathString = path.toString().replace("\\", "/")
                    // Make it work with Architectury as well
                    .replace("common/build/classes/java/main", "fabric/build/resources/main")
                    .replace("common/build/classes/kotlin/main", "fabric/build/resources/main")
                    .replace("classes/java/main", "resources/main")
                    .replace("classes/kotlin/main", "resources/main");
            Path resourcesPath = Paths.get(resourcesPathString);
            return modMap.get(resourcesPath);
        } else {
            debug(() -> "Mod at path '" + path.toAbsolutePath() + "' is at fault," +
                    " but it could not be found in the map of mod paths: " /*+ modMap*/);
            return Collections.emptySet();
        }
    }
}
