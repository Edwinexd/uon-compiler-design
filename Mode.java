public enum Mode {
    UNKNOWN,
    IDENTIFIER,
    NUMEIRC, // int, + potential floats
    FLOAT,
    STRING,
    DELIMITER,
    INVALID,
    INVALID_STRING, // it's own thing as it has a special error format
    LINECOMMENT,
    BLOCKCOMMENT,
    EOF;

    public static boolean isComment(Mode mode) {
        return mode == LINECOMMENT || mode == BLOCKCOMMENT;
    }
}
