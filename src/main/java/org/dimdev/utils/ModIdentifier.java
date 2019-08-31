package org.dimdev.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ModIdentifier {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Set<ModMetadata> identifyFromStacktrace(Throwable e) {
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

    public static Set<ModMetadata> identifyFromClass(String className) {
        return identifyFromClass(className, makeModMap());
    }

    // TODO: get a list of mixin transformers that affected the class and blame those too
    private static Set<ModMetadata> identifyFromClass(String className, Map<URI, Set<ModMetadata>> modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            return Collections.emptySet();
        }

        // Get the URL of the class
        URL url = ModIdentifier.class.getResource(className);
        if (url == null) {
            LOGGER.warn("Failed to identify mod for " + className);
            return Collections.emptySet();
        }

        // Get the mod containing that class
        try {
            return modMap.get(jarFromUrl(url));
        } catch (URISyntaxException | IOException ex) {
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
