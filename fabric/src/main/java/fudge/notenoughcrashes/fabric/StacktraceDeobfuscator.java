package fudge.notenoughcrashes.fabric;

public final class StacktraceDeobfuscator {


    public static void init() {
    }


    public static void deobfuscateThrowable(Throwable t) {
    }


    // No need to insert multiple watermarks in one exception
    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace, boolean insertWatermark) {
        return stackTrace;
    }
}
