package fudge.notenoughcrashes.stacktrace;

import fudge.notenoughcrashes.ModConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.NecPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Consumer;

public final class ModIdentifier {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Set<CommonModMetadata> identifyFromStacktrace(Throwable e) {
        Set<CommonModMetadata> mods = new HashSet<>();
        // Include suppressed exceptions too
        visitChildrenThrowables(e, throwable -> mods.addAll(identifyFromThrowable(throwable)));
        return mods;
    }

    private static void visitChildrenThrowables(Throwable e, Consumer<Throwable> visitor) {
        visitor.accept(e);
        for (Throwable child : e.getSuppressed()) visitChildrenThrowables(child, visitor);
    }

    private static Set<CommonModMetadata> identifyFromThrowable(Throwable e) {
        Map<URI, Set<CommonModMetadata>> modMap = NecPlatform.instance().getModsAtLocationsInDisk();

        // Get the set of classes
        Set<String> classes = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                classes.add(element.getClassName());
            }
            e = e.getCause();
        }

        Set<CommonModMetadata> mods = new LinkedHashSet<>();
        for (String className : classes) {
            Set<CommonModMetadata> classMods = identifyFromClass(className, modMap);
            if (classMods != null) {
                mods.addAll(classMods);
            }
        }
        return mods;
    }

    private static void debug(String message) {
        if (ModConfig.instance().debugModIdentification) NotEnoughCrashes.LOGGER.info(message);
    }

    // TODO: get a list of mixin transformers that affected the class and blame those too
    private static Set<CommonModMetadata> identifyFromClass(String className, Map<URI, Set<CommonModMetadata>> modMap) {
        debug("Analyzing " + className);
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            debug("Ignoring class " + className + " for identification because it is a mixin class");
            return Collections.emptySet();
        }

        try {
            // Get the URL of the class
            Class<?> clazz = Class.forName(className);
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                debug("Ignoring class " + className + " for identification because the code source could not be found");
                return Collections.emptySet(); // Some internal native sun classes
            }
            URL url = codeSource.getLocation();

            if (url == null) {
                LOGGER.warn("Failed to identify mod for " + className);
                return Collections.emptySet();
            }

            URI jar = jarFromUrl(url);
            Set<CommonModMetadata> metadata = modMap.get(jar);
            // Forge tends to give modjar://crashmod type urls, so we try to figure out the mod based on that.
            if (metadata == null && jar.toString().startsWith("modjar://")) {
                metadata = new HashSet<>(NecPlatform.instance().getModMetadatas(jar.toString().substring("modjar://".length())));
            }


            // For some reason loader gives the wrong location with kotlin mods in dev so we need to change it a bit
            if (metadata == null) {
                String oldPath = jar.getPath();
                if (oldPath.length() > "classes/kotlin/main/".length()) {
                    String fixedPath = oldPath.substring(0, oldPath.length() - "classes/kotlin/main/".length()) + "resources/main/";
                    metadata = modMap.get(new File(fixedPath).toURI());
                }
            }


            // Get the mod containing that class
            return metadata;
        } catch (URISyntaxException | IOException | ClassNotFoundException | NoClassDefFoundError e) {
            debug("Ignoring class " + className + " for identification because an error occurred");
            if (ModConfig.instance().debugModIdentification) {
                e.printStackTrace();
            }
            return Collections.emptySet(); // we cannot do it
        }
    }


    public static URI jarFromUrl(URL url) throws URISyntaxException, IOException {
        if (url.getProtocol().equals("jar")) {
            url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
        }
        return url.toURI().normalize();
    }
}
