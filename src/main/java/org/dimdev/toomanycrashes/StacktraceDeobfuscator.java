package org.dimdev.toomanycrashes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class StacktraceDeobfuscator {
    private static final String MAPPINGS_URL = "https://gist.githubusercontent.com/Runemoro/cc6ad843f5403b870214ae34baaa4b60/raw/c7be9a0d5be49eaa365f915fd6326c1ddd419bbd/yarn-mappings.csv";
    private static final boolean DEBUG_IN_DEV = false; // Makes this deobf -> obf for testing in dev. Don't forget to set to false when done!
    private static HashMap<String, String> mappings = null;

    /**
     * If the file does not exits, downloads latest method mappings and saves them to it.
     * Initializes a HashMap between obfuscated and deobfuscated names from that file.
     */
    public static void init(File mappingsFile) {
        if (mappings != null) return;

        // Download the file if necessary
        if (!mappingsFile.exists()) {
            try {
                try (InputStream is = new URL(MAPPINGS_URL).openStream()) {
                    Files.copy(is, mappingsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Read the mapping
        HashMap<String, String> mappings = new HashMap<>();
        try (Scanner scanner = new Scanner(mappingsFile)) {
            scanner.nextLine(); // Skip CSV header
            while (scanner.hasNext()) {
                String[] mappingLine = scanner.nextLine().split(",");
                String obfName = mappingLine[0];
                String deobfName = mappingLine[1];

                if (!DEBUG_IN_DEV) {
                    mappings.put(obfName, deobfName);
                } else {
                    mappings.put(deobfName, obfName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StacktraceDeobfuscator.mappings = mappings;
    }

    public static void deobfuscateThrowable(Throwable t) {
        Deque<Throwable> queue = new ArrayDeque<>();
        queue.add(t);
        while (!queue.isEmpty()) {
            t = queue.remove();
            t.setStackTrace(deobfuscateStacktrace(t.getStackTrace()));
            if (t.getCause() != null) queue.add(t.getCause());
            Collections.addAll(queue, t.getSuppressed());
        }
    }

    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace) {
        if (mappings == null) {
            return stackTrace;
        }

        int index = 0;
        for (StackTraceElement el : stackTrace) {
            stackTrace[index++] = new StackTraceElement(
                    mappings.getOrDefault(el.getClassName(), el.getClassName()),
                    mappings.getOrDefault(el.getMethodName(), el.getMethodName()),
                    el.getFileName(),
                    el.getLineNumber()
            );
        }
        return stackTrace;
    }

    public static void main(String[] args) {
        init(new File("mappings.csv"));
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            System.out.println(entry.getKey() + " <=> " + entry.getValue());
        }
    }
}
