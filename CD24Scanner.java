import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

// TODO: listing file not created!

public class CD24Scanner {
    private int currentLine = 1;
    private int currentColumn = 1;
    private StringBuffer buffer = new StringBuffer();
    // Since we only create one TUNDF for chaines of invalid characters
    // we store them in a single buffer and flush it before creating the next valid token
    private StringBuffer invalidBuffer = new StringBuffer();
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
        complete();
    }

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
            if (Mode.isComment(mode)) {
                return;
            }
            if (mode == Mode.UNKNOWN || mode == Mode.INVALID) {
                tokenizeBuffer();
                mode = Mode.IDENTIFIER;
                buffer.append(c);
                return;
            }
            if (mode == Mode.IDENTIFIER || mode == Mode.STRING) {
                buffer.append(c);
                return;
            }
            if (mode == Mode.DELIMITER) {
                tokenizeBuffer();
                mode = Mode.IDENTIFIER;
                buffer.append(c);
                return;
            }
            System.out.println("No action for mode, alphabetic: " + mode);
        } else if (Character.isDigit(c)) {
            if (Mode.isComment(mode)) {
                return;
            }
            if (mode == Mode.IDENTIFIER || mode == Mode.STRING) {
                buffer.append(c);
                return;
            }
            if (mode == Mode.NUMEIRC || mode == Mode.FLOAT) {
                buffer.append(c);
                return;
            }
            if (mode == Mode.DELIMITER || mode == Mode.UNKNOWN || mode == Mode.INVALID) {
                tokenizeBuffer();
                mode = Mode.NUMEIRC;
                buffer.append(c);
                return;
            }
            System.out.println("No action for mode, digit: " + mode);
        } else if (CharacterTypes.isWhitespace(c)) {
            if (mode == Mode.LINECOMMENT && c == '\n') {
                mode = Mode.UNKNOWN;
                return;
            }
            if (Mode.isComment(mode)) {
                return;
            }
            if (mode == Mode.UNKNOWN) {
                return;
            }
            if (mode == Mode.STRING && c != '\n') {
                buffer.append(c);
                return;
            }
            if (mode == Mode.STRING && c == '\n') {
                mode = Mode.INVALID;
                invalidateBuffer();
                tokenizeBuffer();
                return;
            }
            if (mode == Mode.IDENTIFIER || mode == Mode.NUMEIRC || mode == Mode.FLOAT || mode == Mode.DELIMITER || mode == Mode.INVALID) {
                tokenizeBuffer();
                return;
            }
            System.out.println("No action for mode, whitespace: " + mode);
        } else if (CharacterTypes.isDelimiter(c) || c == '!') {
            if (mode == Mode.LINECOMMENT) {
                return;
            }
            if (mode == Mode.BLOCKCOMMENT) {
                if (c == '*' && buffer.length() < 2) {
                    buffer.append(c);
                    return;
                } else if (c == '/' && buffer.length() == 2) {
                    buffer.delete(0, buffer.length());
                    mode = Mode.UNKNOWN;
                    return;
                } else {
                    buffer.delete(0, buffer.length());
                    return;
                }
            }
            if (mode == Mode.DELIMITER || mode == Mode.STRING) {
                buffer.append(c);
                return;
            } else if (mode == Mode.NUMEIRC && c == '.') {
                mode = Mode.FLOAT;
                buffer.append(c);
                return;
            } else if (mode == Mode.IDENTIFIER || mode == Mode.NUMEIRC || mode == Mode.FLOAT) {
                tokenizeBuffer();
                mode = Mode.DELIMITER;
                buffer.append(c);
                return;
            } else if (mode == Mode.UNKNOWN || mode == Mode.INVALID) {
                tokenizeBuffer();
                mode = Mode.DELIMITER;
                buffer.append(c);
                return;
            }
            System.out.println("No action for mode, delimiter: " + mode);
            
        } else if (c == 13) {
            // Fuck windows
        } else if (c == '"') {
            if (mode != Mode.STRING && !Mode.isComment(mode)) {
                tokenizeBuffer();
                buffer.append(c);
                mode = Mode.STRING;
                return;
            }
            if (mode == Mode.STRING) {
                // string termination character
                buffer.append(c);
                tokenizeBuffer();
            }
            return;
        }
        else {
            if (Mode.isComment(mode)) {
                return; // Ignored
            } else if (mode == Mode.STRING) {
                buffer.append(c);
                return;
            } else if (mode != Mode.INVALID) {
                tokenizeBuffer();
                feedInvalid(c);
                mode = Mode.INVALID;
                return;
            } else {
                feedInvalid(c);
                return;
            }
            // System.out.println("No action for character: " + c + (int)c);
        }

    }

    public void complete() {
        /*
         * Semantic meaning of reaching end of file
         */
        tokenizeBuffer();
        mode = Mode.EOF;
        tokenizeBuffer();
    }

    private void invalidateBuffer() {
        invalidBuffer.append(buffer);
        buffer.delete(0, buffer.length());
    }

    private void feedInvalid(char c) {
        invalidBuffer.append(c);
    }

    private void tokenizeInvalid() {
        /*
         * Should be called just before a valid token is created
         */
        if (invalidBuffer.length() == 0) {
            return;
        }
        Token t = new Token(TokenType.TUNDF, invalidBuffer.toString(), currentLine, currentColumn);
        System.out.println(t);
        invalidBuffer.delete(0, invalidBuffer.length());
    }

    private void createToken(TokenType type, String lexeme, int bufferPosition) {
        tokenizeInvalid();
        Token t = new Token(type, lexeme, currentLine, currentColumn - bufferPosition);
        System.out.println(t);
        tokens.add(t);
        tokenOutput.write(t);
    }

    // TODO: This is stupid
    private TokenType getIdentifierType(String lexeme) {
        String lowerd = lexeme.toLowerCase();
        if (lowerd.equals("cd24")) {
            return TokenType.TCD24;
        } else if (lowerd.equals("const")) {
            return TokenType.TCONS;
        } else if (lowerd.equals("typedef")) {
            return TokenType.TTYPD;
        } else if (lowerd.equals("def")) {
            return TokenType.TTDEF;
        } else if (lowerd.equals("arraydef")) {
            return TokenType.TARRD;
        } else if (lowerd.equals("main")) {
            return TokenType.TMAIN;
        } else if (lowerd.equals("begin")) {
            return TokenType.TBEGN;
        } else if (lowerd.equals("end")) {
            return TokenType.TTEND;
        } else if (lowerd.equals("array")) {
            return TokenType.TARAY;
        } else if (lowerd.equals("tof")) {
            return TokenType.TTTOF;
        } else if (lowerd.equals("func")) {
            return TokenType.TFUNC;
        } else if (lowerd.equals("void")) {
            return TokenType.TVOID;
        } else if (lowerd.equals("const")) {
            return TokenType.TCNST;
        } else if (lowerd.equals("int")) {
            return TokenType.TINTG;
        } else if (lowerd.equals("float")) {
            return TokenType.TFLOT;
        } else if (lowerd.equals("bool")) {
            return TokenType.TBOOL;
        } else if (lowerd.equals("for")) {
            return TokenType.TTFOR;
        } else if (lowerd.equals("repeat")) {
            return TokenType.TREPT;
        } else if (lowerd.equals("until")) {
            return TokenType.TUNTL;
        } else if (lowerd.equals("do")) {
            return TokenType.TTTDO;
        } else if (lowerd.equals("while")) {
            return TokenType.TWHIL;
        } else if (lowerd.equals("if")) {
            return TokenType.TIFTH;
        } else if (lowerd.equals("else")) {
            return TokenType.TELSE;
        } else if (lowerd.equals("elif")) {
            return TokenType.TELIF;
        } else if (lowerd.equals("switch")) {
            return TokenType.TSWTH;
        } else if (lowerd.equals("case")) {
            return TokenType.TCASE;
        } else if (lowerd.equals("default")) {
            return TokenType.TDFLT;
        } else if (lowerd.equals("break")) {
            return TokenType.TBREK;
        } else if (lowerd.equals("input")) {
            return TokenType.TINPT;
        } else if (lowerd.equals("print")) {
            return TokenType.TPRNT;
        } else if (lowerd.equals("printline")) {
            return TokenType.TPRLN;
        } else if (lowerd.equals("return")) {
            return TokenType.TRETN;
        } else if (lowerd.equals("not")) {
            return TokenType.TNOTT;
        } else if (lowerd.equals("and")) {
            return TokenType.TTAND;
        } else if (lowerd.equals("or")) {
            return TokenType.TTTOR;
        } else if (lowerd.equals("xor")) {
            return TokenType.TTXOR;
        } else if (lowerd.equals("true")) {
            return TokenType.TTRUE;
        } else if (lowerd.equals("false")) {
            return TokenType.TFALS;
        }
        return TokenType.TIDEN;
    }
    
    private TokenType getTokenTypeOf(char c) {
        // TODO: Use hashmap (or technically a straight up array could work)
        if (c == ',') {
            return TokenType.TCOMA;
        } else if (c == ';') {
            return TokenType.TSEMI;
        } else if (c == '[') {
            return TokenType.TLBRK;
        } else if (c == ']') {
            return TokenType.TRBRK;
        } else if (c == '(') {
            return TokenType.TLPAR;
        } else if (c == ')') {
            return TokenType.TLBRK;
        } else if (c == '=') {
            return TokenType.TEQUL;
        } else if (c == '+') {
            return TokenType.TPLUS;
        } else if (c == '-') {
            return TokenType.TMINS;
        } else if (c == '*') {
            return TokenType.TSTAR;
        } else if (c == '/') {
            return TokenType.TDIVD;
        } else if (c == '%') {
            return TokenType.TPERC;
        } else if (c == '^') {
            return TokenType.TCART;
        } else if (c == '<') {
            return TokenType.TLESS;
        } else if (c == '>') {
            return TokenType.TGRTR;
        } else if (c == ':') {
            return TokenType.TCOLN;
        } else if (c == '.') {
            return TokenType.TDOTT;
        }
        return null;
    }

    private void tokenizeConsumeDelimiters() {
        if (buffer.length() > 2) {
            for (int i = 0; i < buffer.length()-2; i++) {
                char c1 = buffer.charAt(i);
                char c2 = buffer.charAt(i+1);
                char c3 = buffer.charAt(i+2);
                if (c1 == '/' && c2 == '-' && c3 == '-') {
                    // Line comment
                    mode = Mode.LINECOMMENT;
                    buffer.delete(i, buffer.length());
                    break;
                } else if (c1 == '/' && c2 == '*' && c3 == '*') {
                    // Block comment
                    mode = Mode.BLOCKCOMMENT;
                    buffer.delete(i, buffer.length());
                    break;
                }
            }
        }

        while (buffer.length() > 1) {
            // Taking first two characters and trying to match them to a delimiter / operator
            char c1 = buffer.charAt(0);
            char c2 = buffer.charAt(1);

            TokenType tType = null;
            if (c1 == '<' && c2 == '=') {
                tType = TokenType.TLEQL;
            } else if (c1 == '>' && c2 == '=') {
                tType = TokenType.TGEQL;
            } else if (c1 == '!' && c2 == '=') {
                tType = TokenType.TNEQL;
            } else if (c1 == '=' && c2 == '=') {
                tType = TokenType.TEQEQ;
            } else if (c1 == '+' && c2 == '=') {
                tType = TokenType.TPLEQ;
            } else if (c1 == '-' && c2 == '=') {
                tType = TokenType.TMNEQ;
            } else if (c1 == '*' && c2 == '=') {
                tType = TokenType.TSTEQ;
            } else if (c1 == '/' && c2 == '=') {
                tType = TokenType.TDVEQ;
            }

            if (tType != null) {
                createToken(tType, null, 0);
                buffer.delete(0, 2);
                continue;
            }

            // Couldn't match two characters, trying with one
            char c = buffer.charAt(0);
            tType = getTokenTypeOf(c);
            if (tType != null) {
                createToken(tType, null, 0);
                buffer.deleteCharAt(0);
                continue;
            } else {
                feedInvalid(c);
                buffer.deleteCharAt(0);
                continue;
            }
        }

        if (buffer.length() == 0) {
            if (Mode.isComment(mode)) {
                tokenizeInvalid();
            }
            return;
        }
        // Last character
        char c = buffer.charAt(0);
        TokenType tType = getTokenTypeOf(c);
        if (tType != null) {
            createToken(tType, null, 0);
            buffer.deleteCharAt(0);
            return;
        }
        feedInvalid(c);
        // If we entred a comment mode, invalid characters should be tokenized immediately
        // and not "overflow" to after the comment
        if (Mode.isComment(mode)) {
            tokenizeInvalid();
        }
    }

    private void tokenizeBuffer() {
        // When we have reached a differnt type of characters and need to tokenize
        // whatever is in the buffer before continuing
        Mode oldMode = mode;
        if (mode == Mode.UNKNOWN || buffer.length() == 0 && mode != Mode.EOF) {
            return;
        }
        if (mode == Mode.IDENTIFIER) {
            TokenType type = getIdentifierType(buffer.toString());
            createToken(type, type == TokenType.TIDEN ? buffer.toString() : null, 0);
        } else if (mode == Mode.DELIMITER) {
            tokenizeConsumeDelimiters();
        } else if (mode == Mode.FLOAT) {
            if (buffer.charAt(buffer.length()-1) == '.') {
                // Not a float, ended with a dot so will correspond to two tokens
                // int + dot
                String intLexeme = buffer.substring(0, buffer.length()-2);
                createToken(TokenType.TILIT, intLexeme, 0);
                createToken(TokenType.TDOTT, null, buffer.length()-1);
            } else {
                createToken(TokenType.TFLIT, buffer.toString(), 0);
            }
        } else if (mode == Mode.NUMEIRC) {
            createToken(TokenType.TILIT, buffer.toString(), 0);
        } else if (mode == Mode.STRING) {
            // skipping first and last character
            createToken(TokenType.TSTRG, buffer.substring(1, buffer.length()-1), 0);
        } else if (mode == Mode.INVALID) {
            // Explicitly requesting to tokenize invalid buffer
            // to keep everything consistent, buffer is invalidated i.e.
            // appended to invalidBuffer and then that is tokenized
            invalidateBuffer();
            tokenizeInvalid();
        } else if (mode == Mode.EOF) {
            createToken(TokenType.TTEOF, null, 0);
        } else {
            throw new RuntimeException("Invalid mode " + mode + " for tokenizing buffer, not implemented!");
        }

        buffer.delete(0, buffer.length());
        // Resetting mode if it was not changed, tokenizeConsumeDelimiters
        // might have changed it to a comment mode
        if (mode == oldMode) {
            mode = Mode.UNKNOWN;
        }
    }
}
