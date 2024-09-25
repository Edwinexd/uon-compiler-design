import java.io.IOException;
import java.nio.file.Paths;

/**
 * Launcher for Scanner + Parser
 * 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class A1 {
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

        // Cleanup
        tokenOutput.closeWriters();

    }
}
