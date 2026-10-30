package fudge.notenoughcrashes.mixinhandlers;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RecoveryExclusionsTest {
    private static final IllegalStateException VK_TIMEOUT =
            new IllegalStateException("5s timeout reached when waiting for VK semaphore: Queue 0x216eec244b0");

    @Test public void matchesClassAndMessageText() {
        assertTrue(RecoveryExclusions.matches(VK_TIMEOUT, List.of("java.lang.IllegalStateException:waiting for VK semaphore")));
        assertTrue(RecoveryExclusions.matches(VK_TIMEOUT, List.of("java.lang.IllegalStateException")));
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, List.of("java.lang.IllegalStateException:something else")));
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, List.of("java.lang.IllegalStateException:waiting for vk semaphore")));
        assertFalse(RecoveryExclusions.matches(new IllegalStateException(), List.of("java.lang.IllegalStateException:waiting")));
    }

    @Test public void requiresExactFullyQualifiedClass() {
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, List.of("IllegalStateException")));
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, List.of("java.lang.RuntimeException")));
    }

    @Test public void searchesCauseChainButNotSuppressed() {
        var wrapped = new RuntimeException("Render Frame", VK_TIMEOUT);
        assertTrue(RecoveryExclusions.matches(wrapped, List.of("java.lang.IllegalStateException:VK semaphore")));

        var withSuppressed = new RuntimeException("outer");
        withSuppressed.addSuppressed(new NullPointerException("commandBuffer is null"));
        assertFalse(RecoveryExclusions.matches(withSuppressed, List.of("java.lang.NullPointerException")));
    }

    @Test public void ignoresBlankAndMalformedEntries() {
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, List.of("", "  ", ":waiting for VK semaphore")));
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, List.of()));
        assertFalse(RecoveryExclusions.matches(VK_TIMEOUT, null));
    }

    @Test public void handlesCauseCycles() {
        var first = new RuntimeException("first");
        var second = new RuntimeException("second", first);
        first.initCause(second);
        assertFalse(RecoveryExclusions.matches(first, List.of("java.lang.IllegalStateException")));
        assertTrue(RecoveryExclusions.matches(first, List.of("java.lang.RuntimeException:second")));
    }
}
