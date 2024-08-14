import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

// TODO: listing file not created!
// TODO: Column numbers are incorrect
// TODO: currentLine and currentColumn are not updated at all
// 

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

    public void scan(String path) throws IOException {
        try (Scanner scanner = new Scanner(new File(path))) {
            scanner.useDelimiter("");
            while (scanner.hasNext()) {
                feed(scanner.next().charAt(0));
            }
        }
        complete();
    }

    public void feed(char c) throws IOException {
        handleChar(c);
        if (c == 13) {
            return;
        }
        tokenOutput.feedChar(c);
        if (c == '\n') {
            currentLine++;
            currentColumn = 1;
            return;
        }
        if (c == '\t') {
            currentColumn += 4;
            return;
        }
        currentColumn++;
    }

    private void handleChar(char c) {
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
                // Reset any "progress" towards ending the block comment
                if (mode == Mode.BLOCKCOMMENT && buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
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
                // Reset any "progress" towards ending the block comment
                if (mode == Mode.BLOCKCOMMENT && buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
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
                // Reset any "progress" towards ending the block comment
                if (mode == Mode.BLOCKCOMMENT && buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
                return;
            }
            if (mode == Mode.UNKNOWN && c != '\n') {
                return;
            }
            if (mode == Mode.STRING && c != '\n') {
                buffer.append(c);
                return;
            }
            if (mode == Mode.STRING && c == '\n') {
                mode = Mode.INVALID_STRING;
                tokenizeBuffer();
                return;
            }
            if (mode == Mode.IDENTIFIER || mode == Mode.NUMEIRC || mode == Mode.FLOAT || mode == Mode.DELIMITER || mode == Mode.INVALID || mode == Mode.UNKNOWN) {
                boolean isNewLine = c == '\n';
                tokenizeBuffer(isNewLine);
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
                // We don't tokenize immediatly if it is ! as it may be part of a large invalid lexeme in the current context
                if (c != '!') {
                    tokenizeBuffer();
                }
                mode = Mode.DELIMITER;
                buffer.append(c);
                return;
            }
            System.out.println("No action for mode, delimiter: " + mode);
            
        } else if (c == 13) {
            // Fuck windows
        } else if (c == '"') {
            // Reset any "progress" towards ending the block comment
            if (mode == Mode.BLOCKCOMMENT && buffer.length() > 0) {
                buffer.delete(0, buffer.length());
            }

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
                // Reset any "progress" towards ending the block comment
                if (mode == Mode.BLOCKCOMMENT && buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
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

    private void tokenizeInvalid(int tentativeOffset) {
        /*
         * Should be called just before a valid token is created
         */
        if (invalidBuffer.length() == 0) {
            return;
        }
        Token t = new Token(TokenType.TUNDF, invalidBuffer.toString(), currentLine, currentColumn - invalidBuffer.length() - tentativeOffset, "lexical error");
        tokens.add(t);
        tokenOutput.write(t);
        invalidBuffer.delete(0, invalidBuffer.length());
        tokenOutput.feedError(t.getErrorFormatted().get());
    }

    private void tokenizeInvalid() {
        tokenizeInvalid(0);
    }

    private void createToken(TokenType type, String lexeme, int column, int tentativeOffset, String error) {
        tokenizeInvalid(tentativeOffset);
        Token t = new Token(type, lexeme, currentLine, column, error);
        // System.out.println(t);
        tokens.add(t);
        tokenOutput.write(t);
        if (error != null) {
            tokenOutput.feedError(t.getErrorFormatted().get());
        }
    }

    private void createToken(TokenType type, String lexeme, int column) {
        createToken(type, lexeme, column, 0, null);
    }

    private void createToken(TokenType type, String lexeme, int column, int tentativeOffset) {
        createToken(type, lexeme, column, tentativeOffset, null);
    }
    
    private void createToken(TokenType type, String lexeme, int column, String error) {
        createToken(type, lexeme, column, 0, error);
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

            Optional<TokenType> tType = TokenType.fromDelimiter(c1, c2);
            if (tType.isPresent()) {
                createToken(tType.get(), null, currentColumn - buffer.length(), buffer.length());
                buffer.delete(0, 2);
                continue;
            }

            // Couldn't match two characters, trying with one
            char c = buffer.charAt(0);
            tType = TokenType.fromDelimiter(c);
            if (tType.isPresent()) {
                createToken(tType.get(), null, currentColumn - buffer.length(), buffer.length());
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
        Optional<TokenType> tType = TokenType.fromDelimiter(c);
        if (tType.isPresent()) {
            createToken(tType.get(), null, currentColumn - buffer.length(), buffer.length());
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

    private void tokenizeBuffer(boolean forceDumpErrorBuffer) {
        // When we have reached a differnt type of characters and need to tokenize
        // whatever is in the buffer before continuing
        Mode oldMode = mode;
        if (mode == Mode.UNKNOWN || buffer.length() == 0 && mode != Mode.EOF && invalidBuffer.length() == 0) {
            return;
        }
        if (mode == Mode.IDENTIFIER) {
            TokenType type = TokenType.fromIdentifier(buffer.toString());
            createToken(type, type == TokenType.TIDEN ? buffer.toString() : null, currentColumn - buffer.length());
        } else if (mode == Mode.DELIMITER) {
            tokenizeConsumeDelimiters();
        } else if (mode == Mode.FLOAT) {
            if (buffer.charAt(buffer.length()-1) == '.') {
                // Not a float, ended with a dot so will correspond to two tokens
                // int + dot
                String intLexeme = buffer.substring(0, buffer.length()-1);
                try {
                    Integer.parseInt(intLexeme);
                    createToken(TokenType.TILIT, intLexeme, currentColumn - buffer.length());
                } catch (NumberFormatException e) {
                    createToken(TokenType.TUNDF, intLexeme, currentColumn - buffer.length(), "lexical error: numeric literal overflow");
                }
                createToken(TokenType.TDOTT, null, currentColumn - (buffer.length()-intLexeme.length()));
            } else {
                Double d = Double.parseDouble(buffer.toString());
                // I.e. overflow
                if (d.isInfinite()) {
                    createToken(TokenType.TUNDF, buffer.toString(), currentColumn - buffer.length(), "lexical error: numeric literal overflow");
                } else {
                    createToken(TokenType.TFLIT, buffer.toString(), currentColumn - buffer.length());
                }
            }
        } else if (mode == Mode.NUMEIRC) {
            try {
                Integer.parseInt(buffer.toString());
                createToken(TokenType.TILIT, buffer.toString(), currentColumn - buffer.length());
            } catch (NumberFormatException e) {
                createToken(TokenType.TUNDF, buffer.toString(), currentColumn - buffer.length(), "lexical error: numeric literal overflow");
            }
        } else if (mode == Mode.STRING) {
            // skipping first and last character for lexeme
            // TODO: This is off by one for some reason
            createToken(TokenType.TSTRG, buffer.substring(1, buffer.length()-1), currentColumn - buffer.length());
        } else if (mode == Mode.INVALID) {
            // Explicitly requesting to tokenize invalid buffer
            // to keep everything consistent, buffer is invalidated i.e.
            // appended to invalidBuffer and then that is tokenized
            invalidateBuffer();
            tokenizeInvalid();
        } else if (mode == Mode.INVALID_STRING) {
            createToken(TokenType.TUNDF, buffer.toString(), currentColumn - buffer.length(), "lexical error: unterminated string");
        } else if (mode == Mode.EOF) {
            createToken(TokenType.TTEOF, null, 0);
        } else {
            throw new RuntimeException("Invalid mode " + mode + " for tokenizing buffer, not implemented or not applicable!");
        }

        buffer.delete(0, buffer.length());
        // Resetting mode if it was not changed, tokenizeConsumeDelimiters
        // might have changed it to a comment mode
        if (mode == oldMode) {
            mode = Mode.UNKNOWN;
        }

        // Used to force dump the error buffer for e.x. newline characters as tokens don't flow between lines
        if (forceDumpErrorBuffer) {
            tokenizeInvalid();
        }
    }

    private void tokenizeBuffer() {
        tokenizeBuffer(false);
    }
}
