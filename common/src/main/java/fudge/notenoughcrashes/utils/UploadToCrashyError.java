package fudge.notenoughcrashes.utils;

abstract class UploadToCrashyError extends RuntimeException {
    public UploadToCrashyError(String message) {
        super(message);
    }

    static class InvalidCrash extends UploadToCrashyError {
        public InvalidCrash() {
            super("Uploaded crash log is invalid");
        }
    }

    static class TooLarge extends UploadToCrashyError {
        public TooLarge() {
            super("Uploaded crash log is too large");
        }
    }
}


