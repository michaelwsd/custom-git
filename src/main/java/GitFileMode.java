public enum GitFileMode {
    REGULAR_FILE(100644),
    EXECUTABLE_FILE(100755),
    SYMBOLIC_LINK(120000),
    DIRECTORY(40000);

    private final int mode;

    GitFileMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public static boolean isValidMode(int value) {
        for (GitFileMode m : GitFileMode.values()) {
            if (m.mode == value) return true;
        }
        return false;
    }

    public static GitFileMode fromInt(int value) {
        for (GitFileMode m : GitFileMode.values()) {
            if (m.mode == value) return m;
        }
        throw new IllegalArgumentException("Unknown mode: " + value);
    }
}
