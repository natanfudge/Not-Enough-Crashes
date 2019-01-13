package org.dimdev.utils;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public final class ModIdentifier {
    private static final Logger LOGGER = LogManager.getLogger();

    public static Set<ModInfo> identifyFromStacktrace(Throwable e) {
        Map<File, Set<ModInfo>> modMap = makeModMap();

        // Get the set of classes
        Set<String> classes = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                classes.add(element.getClassName());
            }
            e = e.getCause();
        }

        Set<ModInfo> mods = new LinkedHashSet<>();
        for (String className : classes) {
            Set<ModInfo> classMods = identifyFromClass(className, modMap);
            if (classMods != null) mods.addAll(classMods);
        }
        return mods;
    }

    public static Set<ModInfo> identifyFromClass(String className) {
        return identifyFromClass(className, makeModMap());
    }

    // TODO: get a list of mixin transformers that affected the class and blame those too
    private static Set<ModInfo> identifyFromClass(String className, Map<File, Set<ModInfo>> modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) return Collections.emptySet();

        // Get the URL of the class
        URL url = ModIdentifier.class.getResource(className);
        if (url == null) {
            LOGGER.warn("Failed to identify " + className);
            return Collections.emptySet();
        }

        // Get the mod containing that class
        try {
            if (url.getProtocol().equals("jar")) url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
            return modMap.get(new File(url.toURI()).getCanonicalFile());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<File, Set<ModInfo>> makeModMap() {
        Map<File, Set<ModInfo>> modMap = new HashMap<>();
        for (ModContainer mod : FabricLoader.INSTANCE.getModContainers()) {
            Set<ModInfo> currentMods = modMap.getOrDefault(mod.getOriginFile(), new HashSet<>());
            currentMods.add(mod.getInfo());
            try {
                modMap.put(mod.getOriginFile().getCanonicalFile(), currentMods);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return modMap;
    }
}
