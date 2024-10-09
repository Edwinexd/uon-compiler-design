import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Output handler for the scanner and parser, handles filewriters, inserting
 * newlines etc.
 * 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class TokenOutput {
    // resets when you make a newline
    private static final char NEWLINE_CHAR = (char) 10;
    private int writtenCount;
    private int writtenCountParser;
    private FileWriter listingFileWriter;
    private FileWriter tokenOutputWriter;
    private FileWriter parserTokenOutputWriter;
    private int currentLine = 1;
    private LinkedList<String> errors = new LinkedList<String>();
    private LinkedList<String> parserErrors = new LinkedList<String>();
    private LinkedList<String> semanticErrors = new LinkedList<String>();

    public void initializeWriters(String errorPath, String tokenPath, String parserTokenPath) {
        try {
            listingFileWriter = new FileWriter(errorPath);
            tokenOutputWriter = new FileWriter(tokenPath);
            parserTokenOutputWriter = new FileWriter(parserTokenPath);

            printToListingFile(currentLine + ". ");
        } catch (IOException e) {
            System.err.println("Error initializing writer: " + e.getMessage());
        }
    }

    public void closeWriters() {
        try {
            listingFileWriter.close();
            tokenOutputWriter.close();
            parserTokenOutputWriter.close();
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

    public void writeParserNode(SyntaxTreeNode node) {
        if (parserTokenOutputWriter == null) {
            throw new IllegalStateException("Must initialise writers before calling this method");
        }
        String output = node.toString();

        // if we have exceeded 70 characters / "10 columns"
        if (writtenCountParser > 70) {
            appendNewLineParser();
        }

        writeTextParser(output);
    }

    private void writeTextParser(String text) {
        try {
            System.out.print(text);
            parserTokenOutputWriter.write(text);
            writtenCountParser += text.length();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private void appendNewLineParser() {
        writeTextParser("\n");
        writtenCountParser = 0;
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

    public void flushParserErrors() {
        for (String error : parserErrors) {
            printToListingFile("    " + error);
            printToListingFile(NEWLINE_CHAR);
        }
        parserErrors.clear();
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

    public void feedSemanticError(String error) {
        semanticErrors.add(error);
    }
}
