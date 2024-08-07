import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

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
        } else if (Character.isDigit(c)) {
            if (mode == Mode.IDENTIFIER) {
                buffer.append(c);
                return;
            }
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
            } else if (mode == Mode.DELIMITER) {
                
            }
        }
        

    }
}
