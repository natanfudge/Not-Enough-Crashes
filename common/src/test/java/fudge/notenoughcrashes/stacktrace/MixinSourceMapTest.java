package fudge.notenoughcrashes.stacktrace;

import org.junit.Test;
import static org.junit.Assert.*;

public class MixinSourceMapTest {
    private static final String CLASS = "test.Target";
    private void register() {
        MixinSourceMap.register(CLASS, "SMAP\nTarget.java\nMixin\n*S Mixin\n*F\n+ 1 Target.java\ntest/Target.java\n+ 2 ExampleMixin.java\ntest/ExampleMixin.java\n*L\n1#1,100:1\n10#2,20:101\n30#2,3:121,2\n*E\n");
    }
    @Test public void translatesInjectedLinesAndPreservesIdentity() {
        register();
        var frame = new StackTraceElement("loader", "module", "1", CLASS, "injected", "Target.java", 107);
        var mapped = MixinSourceMap.translate(frame);
        assertEquals("ExampleMixin.java", mapped.getFileName());
        assertEquals(16, mapped.getLineNumber());
        assertEquals(CLASS, mapped.getClassName());
        assertEquals("module", mapped.getModuleName());
        assertEquals(mapped, MixinSourceMap.translate(mapped));
        assertEquals(32, MixinSourceMap.translate(new StackTraceElement(CLASS, "x", "Target.java", 126)).getLineNumber());
    }
    @Test public void preservesUnmappedAndNativeFrames() {
        register();
        for (int line : new int[]{-2, 42, 200}) {
            var frame = new StackTraceElement(CLASS, "x", "Target.java", line);
            assertEquals(frame, MixinSourceMap.translate(frame));
        }
        MixinSourceMap.register("invalid", "SMAP\nInvalid.java\nMixin\n*S Mixin\n*F\n+ not-a-number X.java\npath\n*E");
    }
    @Test public void handlesCauseAndSuppressedCycles() {
        register();
        Throwable first = new Exception();
        Throwable second = new Exception(first);
        first.addSuppressed(second);
        first.setStackTrace(new StackTraceElement[]{new StackTraceElement(CLASS, "x", "Target.java", 110)});
        second.setStackTrace(first.getStackTrace());
        MixinSourceMap.translate(first);
        assertEquals("ExampleMixin.java", first.getStackTrace()[0].getFileName());
        assertEquals(19, second.getStackTrace()[0].getLineNumber());
    }
}
