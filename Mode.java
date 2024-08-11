public enum Mode {
    UNKNOWN, // to be fixed
    IDENTIFIER, 
    NUMEIRC, // int, floats, ...
    FLOAT,
    STRING,
    DELIMITER,
    INVALID,
    LINECOMMENT,
    BLOCKCOMMENT,
    EOF;
    // ... more stuff to come

    public static boolean isComment(Mode mode) {
        return mode == LINECOMMENT || mode == BLOCKCOMMENT;
    }
}
