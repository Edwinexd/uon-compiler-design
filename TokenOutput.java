import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class TokenOutput {
    // resets when you make a newline
    private int writtenCount;
    private FileWriter writer;
    private int currentLine = 1;
    private LinkedList<String> errors = new LinkedList<String>();

    /// i feel like the writer opening every time might cause too much overhead

    public void initializeWriter(String path) throws IOException
    {
        try 
        {
            writer = new FileWriter(path);

            printToFile(currentLine + ". ");
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
            writer.close();
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }

    }

    public void write(Token t) 
    {
        // Each line of output, in the absence of errors, will exceed 60 characters in length. Once any
        // line of output has exceeded 60 characters then you should terminate that output line.

        // stream of tokens

        // initialize writer if not initialised
        if (writer == null)
        {
            throw new IllegalAccessError("Initialise it fella");
        }

        String output = t.toString();

        int charCount = output.length();


        // + 6 because according to the spec the lexeme should not wrap and it only matters that the token is in the 60 limit
        if ((writtenCount + 6) < 60)
        {
            // same line + append space
            // printToFile
            System.out.print(t);

            // Incement
            writtenCount += charCount;
        }
        else
        {
            // new line + append space
            System.out.println();
            // printToFile
            System.out.print(t);
            // Incement
            writtenCount = charCount;
            // will there be a space between
        }

    }

    public void feedChar(char character) throws IOException
    {
        if (character == '\n')
        {
            //new line
            printToFile(character);

            // print all errors
            for (String error : errors) {
                //ERROR
                printToFile(error);
                // new line
                printToFile(character);
            }

            if (errors.size() == 0)
            {
                // no errors and there be n new lines
                printToFile(character);
            }

            // clear errors
            errors.clear();

            currentLine++;

            printToFile(currentLine + ". ");
        }
        else
        {
            printToFile(character);
        }
    }

    public void feedError(String error) throws IOException
    {
        errors.add(error);
    }

    private void printToFile(char character) throws IOException
    {
        try 
        {
            writer.write(character);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }

    private void printToFile(String error) throws IOException
    {
        try 
        {
            writer.write(error);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }
}
