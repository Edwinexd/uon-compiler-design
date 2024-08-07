import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

// TODO:
// keywords
// strings
// comments are not handled
// listing file not created
// floats and ints

public class CD24Scanner {
    private int currentLine = 1;
    private int currentColumn = 1;
    private StringBuffer buffer = new StringBuffer();
    private List<Token> tokens = new LinkedList<>();
    private Mode mode = Mode.UNKNOWN;
    private TokenOutput tokenOutput;

    public CD24Scanner(TokenOutput tokenOutput) {
        this.tokenOutput = tokenOutput;
    }

    public void scan(String path) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(path))) {
            scanner.useDelimiter("");
            while (scanner.hasNext()) {
                feed(scanner.next().charAt(0));
            }
        }
    }

    // 

    public void feed(char c) {
        // 1
        // numeric
        // ;
        // mode identifier
        // numeric => identifier
        // 111.a
        // tdint, tdot tident (a)
        // "111.a"
        // string, .... " | eol
        if (Character.isAlphabetic(c)) {
            if (mode == Mode.UNKNOWN) {
                mode = Mode.IDENTIFIER;
                buffer.append(c);
                return;
            }
            if (mode == Mode.IDENTIFIER) {
                buffer.append(c);
                return;
            }
            if (mode == Mode.DELIMITER) {
                if (buffer.isEmpty()) {
                    mode = Mode.IDENTIFIER;
                    buffer.append(c);
                    return;
                }
                // tokenize whatever in buffer
            }
            System.out.println("No action for mode, alphabetic: " + mode);
        } else if (Character.isDigit(c)) {
            if (mode == Mode.IDENTIFIER) {
                buffer.append(c);
                return;
            }
            if (mode == Mode.NUMEIRC) {
                buffer.append(c);
                return;
            }
            if (mode == Mode.DELIMITER) {
                if (!buffer.isEmpty()) {
                    // TODO Tokenize before preceeting
                    throw new IllegalAccessError("No");
                }
                mode = Mode.NUMEIRC;
                buffer.append(c);
                return;
            }
            if (mode == Mode.NUMEIRC) {
                buffer.append(c);
                return;
            }
            System.out.println("No action for mode, digit: " + mode);
        } else if (CharacterTypes.isDelimiter(c)) {
            if (mode == Mode.IDENTIFIER) {
                Token t = new Token(TokenType.TIDEN, buffer.toString(), currentLine, currentColumn);
                System.out.println(t);
                buffer.delete(0, buffer.length());
                if (!CharacterTypes.isWhitespace(c)) {
                    buffer.append(c);
                }
                mode = Mode.DELIMITER;
                return;
            // Some of these are combined to form operators such as: <=, >=, !=, ==, +=, -=, *= /=
            } else if (mode == Mode.DELIMITER) {
                if (c == '=' && buffer.length() == 1) {
                    // can be tokenized
                    char lastChar = buffer.charAt(0);
                    Token t;
                    if (lastChar == '<') {
                        t = new Token(TokenType.TLEQL, null, currentLine, currentColumn);
                    } else if (lastChar == '>') {
                        t = new Token(TokenType.TGEQL, null, currentLine, currentColumn);
                    } else if (lastChar == '!') {
                        t = new Token(TokenType.TNEQL, null, currentLine, currentColumn);
                    } else if (lastChar == '=') {
                        t = new Token(TokenType.TEQEQ, null, currentLine, currentColumn);
                    } else if (lastChar == '+') {
                        t = new Token(TokenType.TPLEQ, null, currentLine, currentColumn);
                    } else if (lastChar == '-') {
                        t = new Token(TokenType.TMNEQ, null, currentLine, currentColumn);
                    } else if (lastChar == '*') {
                        t = new Token(TokenType.TSTEQ, null, currentLine, currentColumn);
                    } else if (lastChar == '/') {
                        t = new Token(TokenType.TDVEQ, null, currentLine, currentColumn);
                    } else {
                        throw new IllegalArgumentException("Invalid delimiter");
                    }
                    buffer.delete(0, buffer.length());
                    System.out.println(t);
                } else if (CharacterTypes.isWhitespace(c)) {
                    if (buffer.length() == 0) {
                        return;
                    }
                    char lastChar = buffer.charAt(0);
                    Token t;
                    if (lastChar == ',') {
                        t = new Token(TokenType.TCOMA, null, currentLine, currentColumn);
                    } else if (lastChar == ';') {
                        t = new Token(TokenType.TSEMI, null, currentLine, currentColumn);
                    } else if (lastChar == '[') {
                        t = new Token(TokenType.TLBRK, null, currentLine, currentColumn);
                    } else if (lastChar == ']') {
                        t = new Token(TokenType.TRBRK, null, currentLine, currentColumn);
                    } else if (lastChar == '(') {
                        t = new Token(TokenType.TLPAR, null, currentLine, currentColumn);
                    } else if (lastChar == ')') {
                        t = new Token(TokenType.TLBRK, null, currentLine, currentColumn);
                    } else if (lastChar == '=') {
                        t = new Token(TokenType.TEQUL, null, currentLine, currentColumn);
                    } else if (lastChar == '+') {
                        t = new Token(TokenType.TPLUS, null, currentLine, currentColumn);
                    } else if (lastChar == '-') {
                        t = new Token(TokenType.TMINS, null, currentLine, currentColumn);
                    } else if (lastChar == '*') {
                        t = new Token(TokenType.TSTAR, null, currentLine, currentColumn);
                    } else if (lastChar == '/') {
                        t = new Token(TokenType.TDIVD, null, currentLine, currentColumn);
                    } else if (lastChar == '%') {
                        t = new Token(TokenType.TPERC, null, currentLine, currentColumn);
                    } else if (lastChar == '^') {
                        t = new Token(TokenType.TCART, null, currentLine, currentColumn);
                    } else if (lastChar == '<') {
                        t = new Token(TokenType.TLESS, null, currentLine, currentColumn);
                    } else if (lastChar == '>') {
                        t = new Token(TokenType.TGRTR, null, currentLine, currentColumn);
                    } else if (lastChar == ':') {
                        t = new Token(TokenType.TCOLN, null, currentLine, currentColumn);
                    } else if (lastChar == '.') {
                        t = new Token(TokenType.TDOTT, null, currentLine, currentColumn);
                    } else {
                        throw new IllegalArgumentException("Invalid delimiter");
                    }
                    System.out.println(t);
                    buffer.delete(0, buffer.length());
                } else {
                    buffer.append(c);
                }
            } else if (mode == Mode.NUMEIRC) {
                if (c == '.') {
                    // TODO: This does not work, we might have 11.11.11
                    // variable for "have read dot?"
                    if (buffer.charAt(buffer.length()-1) == '.') {
                        // TODO Double dot, tokenize
                        return;
                    }
                    buffer.append(c);
                    return;
                }
            }
            System.out.println("No action for mode, delimiter: " + mode);
            
        }

    }
}
