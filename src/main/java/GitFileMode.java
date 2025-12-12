public enum GitFileMode {
    REGULAR_FILE("100644"),
    EXECUTABLE_FILE("100755"),
    SYMBOLIC_LINK("120000"),
    DIRECTORY("40000");

    private final String mode;

    GitFileMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public static boolean isValidMode(String value) {
        for (GitFileMode m : GitFileMode.values()) {
            if (m.mode.equals(value)) return true;
        }
        return false;
    }

    public static GitFileMode fromInt(String value) {
        for (GitFileMode m : GitFileMode.values()) {
            if (m.mode.equals(value)) return m;
        }
        throw new IllegalArgumentException("Unknown mode: " + value);
    }
}
