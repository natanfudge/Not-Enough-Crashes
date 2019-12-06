package fudge.notenoughcrashes.stacktrace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import fudge.notenoughcrashes.ModConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import org.apache.commons.io.FileUtils;

import net.minecraft.MinecraftVersion;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mapping.reader.v2.MappingGetter;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.reader.v2.TinyV2Factory;
import net.fabricmc.mapping.reader.v2.TinyVisitor;

public final class StacktraceDeobfuscator {

    private static final String MAPPINGS_JAR_LOCATION = "mappings/mappings.tiny";
    private static final String NAMESPACE_FROM = "intermediary";
    public static final String NAMESPACE_TO = "named";
    private static final Path CACHED_MAPPINGS = NotEnoughCrashes.DIRECTORY
                    .resolve("mappings-" + new MinecraftVersion().getName() + ".tiny");

    private static Map<String, String> mappings = null;


    private static void downloadAndCacheMappings() {
        String yarnVersion;
        try {
            yarnVersion = YarnVersion.getLatestBuildForCurrentVersion();
        } catch (IOException e) {
            NotEnoughCrashes.LOGGER.error("Could not get latest yarn build for version", e);
            return;
        }

        NotEnoughCrashes.LOGGER.info("Downloading deobfuscation mappings: " + yarnVersion + " for the first launch");

        String encodedYarnVersion = UrlEscapers.urlFragmentEscaper().escape(yarnVersion);
        // Download V2 jar
        String artifactUrl = "https://maven.fabricmc.net/net/fabricmc/yarn/" + encodedYarnVersion + "/yarn-" + encodedYarnVersion + "-v2.jar";

        try {
            Files.createDirectories(NotEnoughCrashes.DIRECTORY);
        } catch (IOException e) {
            NotEnoughCrashes.LOGGER.error("Could not create Not Enough Crashes directory!", e);
            return;
        }

        File jarFile = NotEnoughCrashes.DIRECTORY.resolve("yarn-mappings.jar").toFile();
        jarFile.deleteOnExit();
        try {
            FileUtils.copyURLToFile(new URL(artifactUrl), jarFile);
        } catch (IOException e) {
            NotEnoughCrashes.LOGGER.error("Failed to downloads mappings!", e);
            return;
        }

        try (FileSystem jar = FileSystems.newFileSystem(jarFile.toPath(), null)) {
            Files.copy(jar.getPath(MAPPINGS_JAR_LOCATION), CACHED_MAPPINGS, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            NotEnoughCrashes.LOGGER.error("Failed to extract mappings!", e);
        }
    }

    public static void init() {
        if (!Files.exists(CACHED_MAPPINGS)) downloadAndCacheMappings();
    }

    private static void loadMappings() {
        if (!Files.exists(CACHED_MAPPINGS)) {
            NotEnoughCrashes.LOGGER.warn("Could not download mappings, stack trace won't be deobfuscated");
            return;
        }

        Map<String, String> mappings = new HashMap<>();

        try (BufferedReader mappingReader = Files.newBufferedReader(CACHED_MAPPINGS)) {
            TinyV2Factory.visit(mappingReader, new TinyVisitor() {
                private final Map<String, Integer> namespaceStringToColumn = new HashMap<>();

                private void addMappings(MappingGetter name) {
                    mappings.put(name.get(namespaceStringToColumn.get(NAMESPACE_FROM)).replace('/', '.'),
                                    name.get(namespaceStringToColumn.get(NAMESPACE_TO)).replace('/', '.'));
                }

                @Override
                public void start(TinyMetadata metadata) {
                    namespaceStringToColumn.put(NAMESPACE_FROM, metadata.index(NAMESPACE_FROM));
                    namespaceStringToColumn.put(NAMESPACE_TO, metadata.index(NAMESPACE_TO));
                }

                @Override
                public void pushClass(MappingGetter name) {
                    addMappings(name);
                }

                @Override
                public void pushMethod(MappingGetter name, String descriptor) {
                    addMappings(name);
                }

                @Override
                public void pushField(MappingGetter name, String descriptor) {
                    addMappings(name);
                }
            });

        } catch (IOException e) {
            NotEnoughCrashes.LOGGER.error("Could not load mappings", e);
        }

        StacktraceDeobfuscator.mappings = mappings;
    }

    public static void deobfuscateThrowable(Throwable t) {
        Deque<Throwable> queue = new ArrayDeque<>();
        queue.add(t);
        boolean firstLoop = true;
        while (!queue.isEmpty()) {
            t = queue.remove();
            t.setStackTrace(deobfuscateStacktrace(t.getStackTrace(), firstLoop));
            if (t.getCause() != null) {
                queue.add(t.getCause());
            }
            Collections.addAll(queue, t.getSuppressed());

            firstLoop = false;
        }
    }

    // No need to insert multiple watermarks in one exception
    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace, boolean insertWatermark) {
        if (!ModConfig.instance().deobfuscateStackTrace || FabricLoader.getInstance().isDevelopmentEnvironment()) return stackTrace;
        if (mappings == null) loadMappings();
        if (mappings == null) return stackTrace;

        ArrayList<StackTraceElement> stackTraceList = Lists.newArrayList(stackTrace);
        if (insertWatermark) {
            try {
                stackTraceList.add(0, new StackTraceElement(NotEnoughCrashes.NAME + " deobfuscated stack trace",
                                "", YarnVersion.getLatestBuildForCurrentVersion(), -1));
            } catch (IOException e) {
                NotEnoughCrashes.LOGGER.error("Could not get used yarn version", e);
                return stackTrace;
            }
        }

        int index = 0;

        for (StackTraceElement el : stackTraceList) {
            String remappedClass = mappings.get(el.getClassName());
            String remappedMethod = mappings.get(el.getMethodName());
            stackTraceList.set(index, new StackTraceElement(
                            remappedClass != null ? remappedClass : el.getClassName(),
                            remappedMethod != null ? remappedMethod : el.getMethodName(),
                            remappedClass != null ? getFileName(remappedClass) : el.getFileName(),
                            el.getLineNumber())
            );
            index++;
        }
        return stackTraceList.toArray(new StackTraceElement[] {});
    }

    private static String getFileName(String className) {
        String remappedFile = className;
        int lastDot = className.lastIndexOf('.');
        if (lastDot != -1) {
            remappedFile = remappedFile.substring(lastDot + 1);
        }

        int firstDollar = className.indexOf('$');
        if (firstDollar != -1) {
            remappedFile = remappedFile.substring(0, firstDollar);
        }

        return remappedFile;
    }

    // For testing
    public static void main(String[] args) {
        init();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            System.out.println(entry.getKey() + " <=> " + entry.getValue());
        }
    }
}
