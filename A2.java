import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Launcher for Scanner + Parser
 * 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class A2 {

    private static String getFileNameFromPath(String path) {
        // using nio path lib to get the files actual name and ignoring the path
        // i.e. /dev/null/source.txt -> source.txt
        return Paths.get(path).getFileName().toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Source file name is missing");
        }

        // Create & prepare the output handlers
        TokenOutput tokenOutput = new TokenOutput();

        String fileName = getFileNameFromPath(args[0]);

        tokenOutput.initializeWriters(fileName + ".lst", fileName + "_tokens.txt", fileName + "_parser_tokens.txt");

        // Create and run the scanner
        CD24Scanner scanner = new CD24Scanner(tokenOutput);
        scanner.scan(args[0]);

        // two newlines intenionally since writeParserNode will dump tokens to sysout
        // aswell
        System.out.println("\n");

        // Create parser
        Parser parser = new Parser(new LinkedList<>(scanner.getTokens()), tokenOutput);

        // Parse the tokens
        SyntaxTreeNode rootNode = parser.parse();

        // Traverse creates a linkedlist which is then written to the parser_tokens.txt
        // file
        for (SyntaxTreeNode node : parser.traverse(rootNode)) {
            tokenOutput.writeParserNode(node);
        }

        // Cleanup
        tokenOutput.flushParserErrors();

        tokenOutput.closeWriters();

    }
}
