package fudge.notenoughcrashes.utils;

class SystemExitBlockedException extends SecurityException {
    public SystemExitBlockedException(String s) {
        super(s);
    }
}

