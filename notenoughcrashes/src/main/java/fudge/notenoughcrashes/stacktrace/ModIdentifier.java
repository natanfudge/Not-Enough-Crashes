package fudge.notenoughcrashes.stacktrace;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class ModIdentifier {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Set<ModMetadata> identifyFromStacktrace(Throwable e) {
        Set<ModMetadata> mods = new HashSet<>();
        // Include suppressed exceptions too
        visitChildrenThrowables(e, throwable -> mods.addAll(identifyFromThrowable(throwable)));
        return mods;
    }

    private static void visitChildrenThrowables(Throwable e, Consumer<Throwable> visitor) {
        visitor.accept(e);
        for (Throwable child : e.getSuppressed()) visitChildrenThrowables(child, visitor);
    }

    private static Set<ModMetadata> identifyFromThrowable(Throwable e) {
        Map<URI, Set<ModMetadata>> modMap = makeModMap();

        // Get the set of classes
        Set<String> classes = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                classes.add(element.getClassName());
            }
            e = e.getCause();
        }

        Set<ModMetadata> mods = new LinkedHashSet<>();
        for (String className : classes) {
            Set<ModMetadata> classMods = identifyFromClass(className, modMap);
            if (classMods != null) {
                mods.addAll(classMods);
            }
        }
        return mods;
    }

    // TODO: get a list of mixin transformers that affected the class and blame those too
    private static Set<ModMetadata> identifyFromClass(String className, Map<URI, Set<ModMetadata>> modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            return Collections.emptySet();
        }
        try {
            // Get the URL of the class
            Class<?> clazz = Class.forName(className);
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) return Collections.emptySet(); // Some internal native sun classes
            URL url = codeSource.getLocation();
            if (url == null) {
                LOGGER.warn("Failed to identify mod for " + className);
                return Collections.emptySet();
            }

            URI jar = jarFromUrl(url);
            Set<ModMetadata> metadata = modMap.get(jar);
            // For some reason loader gives the wrong location with kotlin mods in dev so we need to change it a bit
            if (metadata == null) {
                String oldPath = jar.getPath();
                String fixedPath = oldPath.substring(0, oldPath.length() - "classes/kotlin/main/".length()) + "resources/main/";
                metadata = modMap.get(new File(fixedPath).toURI());
            }

            // Get the mod containing that class
            return metadata;
        } catch (URISyntaxException | IOException | ClassNotFoundException ex) {
            return Collections.emptySet(); // we cannot do it
        }
    }

    private static Map<URI, Set<ModMetadata>> makeModMap() {
        Map<URI, Set<ModMetadata>> modMap = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (!(mod instanceof net.fabricmc.loader.ModContainer)) {
                continue;
            }
            try {
                URI modJar = jarFromUrl(((net.fabricmc.loader.ModContainer) mod).getOriginUrl());
                modMap.computeIfAbsent(modJar, f -> new HashSet<>()).add(mod.getMetadata());
            } catch (URISyntaxException | IOException ignored) {
                // cannot find jar, so bruh
            }
        }

        return modMap;
    }

    private static URI jarFromUrl(URL url) throws URISyntaxException, IOException {
        if (url.getProtocol().equals("jar")) {
            url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
        }
        return url.toURI().normalize();
    }
}
