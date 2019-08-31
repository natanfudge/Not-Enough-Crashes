package org.dimdev.toomanycrashes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class StacktraceDeobfuscator {

    public static final String MAPPINGS = "mappings/mappings.tiny";
    private static Map<String, String> mappings = null;

    public static void init() {
        Map<String, String> mappings = new HashMap<>();
        try (BufferedReader mappingReader = new BufferedReader(
                new InputStreamReader(StacktraceDeobfuscator.class.getClassLoader().getResourceAsStream(MAPPINGS)))) {
            //            TinyUtils.read(
            //                    mappingReader,
            //                    "intermediary",
            //                    "named",
            //                    (key, value) -> mappings.put(key.replace('/', '.'), value.replace('/', '.')),
            //                    (intermediary, named) -> mappings.put(intermediary.name, named.name),
            //                    (intermediary, named) -> mappings.put(intermediary.name, named.name)
            //            ); todo no more named
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
            if (t.getCause() != null) {
                queue.add(t.getCause());
            }
            Collections.addAll(queue, t.getSuppressed());
        }
    }

    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace) {
        if (mappings == null) {
            return stackTrace;
        }

        int index = 0;
        for (StackTraceElement el : stackTrace) {
            String remappedClass = mappings.get(el.getClassName());
            String remappedMethod = mappings.get(el.getMethodName());
            stackTrace[index++] = new StackTraceElement(
                    remappedClass != null ? remappedClass : el.getClassName(),
                    remappedMethod != null ? remappedMethod : el.getMethodName(),
                    remappedClass != null ? getFileName(remappedClass) : el.getFileName(),
                    el.getLineNumber()
            );
        }
        return stackTrace;
    }

    public static String getFileName(String className) {
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

    public static void main(String[] args) {
        init();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            System.out.println(entry.getKey() + " <=> " + entry.getValue());
        }
    }
}
