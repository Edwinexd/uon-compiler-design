import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

// TODO: Clean up
public class TokenOutput {
    // resets when you make a newline
    private int writtenCount;
    private FileWriter errorWriter;
    private FileWriter tokenWriter;
    private int currentLine = 1;
    private LinkedList<String> errors = new LinkedList<String>();
    private char newlineChar = (char)10;

    /// i feel like the writer opening every time might cause too much overhead

    public void initializeWriter(String errorPath, String tokenPath) throws IOException
    {
        try 
        {
            errorWriter = new FileWriter(errorPath);
            tokenWriter = new FileWriter(tokenPath);

            printErrorToFile(currentLine + ". ");
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }

    }

    public void closeWriter() throws IOException
    {
        try 
        {
            errorWriter.close();
            tokenWriter.close();
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }

    }

    public void write(Token t) {
        // Must be initialised
        if (errorWriter == null) {
            throw new IllegalStateException("Initialise it fella");
        }

        // Each line of output, in the absence of errors, will exceed 60 characters in length. Once any
        // line of output has exceeded 60 characters then you should terminate that output line.
        if (writtenCount > 60) {
            appendNewLine();
        }
        writeText(t.toString());

        if (t.getType() != TokenType.TUNDF) {
            return;
        }

        appendNewLine();
        writeText("    ");
        writeText(t.getError().get());
        appendNewLine();
    }

    private void writeText(String text) {
        try {
            System.out.print(text);
            tokenWriter.write(text);
            writtenCount += text.length();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private void appendNewLine() {
        writeText("\n");
        writtenCount = 0;
    }

    public void feedChar(char character) throws IOException
    {
        // error File output
        if (character == newlineChar)
        {
            //new line
            printErrorToFile(character);

            // print all errors
            for (String error : errors) {
                //ERROR
                printErrorToFile(error);
                // new line
                printErrorToFile(character);
            }

            // clear errors
            errors.clear();

            currentLine++;

            printErrorToFile(currentLine + ". ");
        }
        else
        {
            printErrorToFile(character);
        }


    }

    public void feedError(String error) throws IOException
    {
        errors.add(error);
    }

    private void printErrorToFile(char character) throws IOException
    {
        try 
        {
            errorWriter.write(character);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }

    private void printErrorToFile(String error) throws IOException
    {
        try 
        {
            errorWriter.write(error);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }
}
