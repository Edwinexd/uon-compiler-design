import java.util.Optional;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    private final String error;

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, line, column, null);
    }

    public Token(TokenType type, String lexeme, int line, int column, String error) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.error = error;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    public Optional<String> getErrorFormatted() {
        if (error == null) {
            return Optional.empty();
        }
        return Optional.of(error + " " + lexeme + " (line " + line + ", column " + column + ")");
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(type.name());
        if (type == TokenType.TSTRG) {
            output.append(" \"");
            output.append(lexeme);
            output.append('"');
            output.append(" ");
        } else if (type == TokenType.TIDEN || type == TokenType.TILIT || type == TokenType.TFLIT) {
            output.append(" ");
            output.append(lexeme);
            output.append(" ");
        }
        int remainder = output.length() % 6;
        if (remainder != 0) {
            for (int i = 0; i < 6 - remainder; i++) {
                output.append(" ");
            }
        }
        return output.toString();
    }
}
