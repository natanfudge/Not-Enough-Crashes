package fudge.notenoughcrashes.upload;

public abstract class UploadToCrashyError extends RuntimeException {
    public UploadToCrashyError(String message) {
        super(message);
    }

    public static class InvalidCrash extends UploadToCrashyError {
        public InvalidCrash() {
            super("Uploaded crash log is invalid");
        }
    }

    public static class TooLarge extends UploadToCrashyError {
        public TooLarge() {
            super("Uploaded crash log is too large");
        }
    }
}


