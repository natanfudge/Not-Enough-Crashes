package io.github.natanfudge.nectest;

public class TestSuppressedCloseable implements AutoCloseable {

    @Override
    public void close() {
        throw new NecTestCrash("Test Suppressed Exception");
    }
}
