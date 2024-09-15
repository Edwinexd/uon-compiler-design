import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class TokenOutput {
    // resets when you make a newline
    private static final char NEWLINE_CHAR = (char) 10;
    private int writtenCount;
    private FileWriter listingFileWriter;
    private FileWriter tokenOutputWriter;
    private int currentLine = 1;
    private LinkedList<String> errors = new LinkedList<String>();
    private LinkedList<String> parserErrors = new LinkedList<String>();

    /// i feel like the writer opening every time might cause too much overhead

    public void initializeWriters(String errorPath, String tokenPath) {
        try {
            listingFileWriter = new FileWriter(errorPath);
            tokenOutputWriter = new FileWriter(tokenPath);

            printToListingFile(currentLine + ". ");
        } catch (IOException e) {
            System.err.println("Error initializing writer: " + e.getMessage());
        }
    }

    public void closeWriters() {
        try {
            listingFileWriter.close();
            tokenOutputWriter.close();
        } catch (IOException e) {
            System.err.println("Error closing writer: " + e.getMessage());
        }
    }

    public void write(Token t) {
        // Must be initialised
        if (listingFileWriter == null) {
            throw new IllegalStateException("Initialise it fella");
        }

        // Each line of output, in the absence of errors, will exceed 60 characters in
        // length. Once any
        // line of output has exceeded 60 characters then you should terminate that
        // output line.
        if (writtenCount > 60) {
            appendNewLine();
        }

        writeText(t.toString());

        if (t.getType() != TokenType.TUNDF) {
            return;
        }

        appendNewLine();
        writeText("    ");
        writeText(t.getErrorFormatted().get());
        appendNewLine();
    }

    private void writeText(String text) {
        try {
            System.out.print(text);
            tokenOutputWriter.write(text);
            writtenCount += text.length();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private void appendNewLine() {
        writeText("\n");
        writtenCount = 0;
    }

    public void feedChar(char character) {
        if (character == NEWLINE_CHAR) {
            printToListingFile(character);

            // printing errors
            for (String error : errors) {
                printToListingFile("    " + error);
                // new line
                printToListingFile(character);
            }

            // clear error buffer
            errors.clear();

            currentLine++;

            printToListingFile(currentLine + ". ");
        } else {
            printToListingFile(character);
        }
    }

    public void feedError(String error) {
        errors.add(error);
    }

    private void printToListingFile(char character) {
        try {
            listingFileWriter.write(character);
        } catch (IOException e) {
            System.err.println("Error writing to listing file: " + e.getMessage());
        }
    }

    private void printToListingFile(String error) {
        try {
            listingFileWriter.write(error);
        } catch (IOException e) {
            System.err.println("Error writing to listing file: " + e.getMessage());
        }
    }

    public void feedParserError(String error) {
        parserErrors.add(error);
    }
}
