package fudge.notenoughcrashes.stacktrace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Translates JSR-45 Mixin line ranges without changing class or method identity. */
public final class MixinSourceMap {
    private static final Map<String, Source> SOURCES = new ConcurrentHashMap<>();
    private static final Pattern LINE = Pattern.compile("(\\d+)(?:#(\\d+))?(?:,(\\d+))?:(\\d+)(?:,(\\d+))?");
    private record Range(String file, int input, int count, int output, int increment) {}
    private record Source(String file, List<Range> ranges) {}

    public static void register(String className, String smap) {
        if (smap == null || !(smap.startsWith("SMAP\n") || smap.startsWith("SMAP\r\n"))) return;
        String[] lines = smap.split("\\r?\\n");
        if (lines.length < 4) return;
        Map<Integer, String> files = new HashMap<>();
        List<Range> ranges = new ArrayList<>();
        boolean mixin = false;
        String section = "";
        int lastFile = 0;
        try {
            for (int i = 3; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("*S ")) { mixin = line.equals("*S Mixin"); section = ""; continue; }
                if (line.startsWith("*")) { section = line; continue; }
                if (!mixin) continue;
                if (section.equals("*F")) {
                    boolean extended = line.startsWith("+ ");
                    String[] parts = (extended ? line.substring(2) : line).split(" ", 2);
                    if (parts.length != 2) continue;
                    files.put(Integer.parseInt(parts[0]), parts[1]);
                    if (extended) i++; // following path is for debuggers; stack traces use the source filename
                } else if (section.equals("*L")) {
                    var matcher = LINE.matcher(line);
                    if (!matcher.matches()) continue;
                    if (matcher.group(2) != null) lastFile = Integer.parseInt(matcher.group(2));
                    int count = matcher.group(3) == null ? 1 : Integer.parseInt(matcher.group(3));
                    int increment = matcher.group(5) == null ? 1 : Integer.parseInt(matcher.group(5));
                    if (files.containsKey(lastFile) && count > 0 && increment > 0) {
                        ranges.add(new Range(files.get(lastFile), Integer.parseInt(matcher.group(1)), count,
                                Integer.parseInt(matcher.group(4)), increment));
                    }
                }
            }
            if (!ranges.isEmpty()) SOURCES.put(className.replace('/', '.'), new Source(lines[1], List.copyOf(ranges)));
        } catch (IllegalArgumentException ignored) {
            // Diagnostics must never turn malformed third-party debug metadata into another crash.
        }
    }

    public static StackTraceElement translate(StackTraceElement frame) {
        Source source = SOURCES.get(frame.getClassName());
        if (source == null || !Objects.equals(source.file, frame.getFileName()) || frame.getLineNumber() < 0) return frame;
        for (Range range : source.ranges) {
            long offset = (long) frame.getLineNumber() - range.output;
            if (offset >= 0 && offset < (long) range.count * range.increment) {
                return new StackTraceElement(frame.getClassLoaderName(), frame.getModuleName(), frame.getModuleVersion(),
                        frame.getClassName(), frame.getMethodName(), range.file, range.input + (int) (offset / range.increment));
            }
        }
        return frame;
    }

    public static void translate(Throwable failure) {
        if (failure == null) return;
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Throwable> pending = new ArrayDeque<>();
        pending.add(failure);
        while (!pending.isEmpty()) {
            Throwable current = pending.remove();
            if (!seen.add(current)) continue;
            StackTraceElement[] trace = current.getStackTrace();
            for (int i = 0; i < trace.length; i++) trace[i] = translate(trace[i]);
            current.setStackTrace(trace);
            if (current.getCause() != null) pending.add(current.getCause());
            Collections.addAll(pending, current.getSuppressed());
        }
    }
}
