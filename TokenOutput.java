import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

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

    public void write(Token t) throws IOException
    {
        // Each line of output, in the absence of errors, will exceed 60 characters in length. Once any
        // line of output has exceeded 60 characters then you should terminate that output line.

        // stream of tokens

        // initialize writer if not initialised
        if (errorWriter == null)
        {
            throw new IllegalAccessError("Initialise it fella");
        }

        String output = t.toString();

        int charCount = output.length();


        // + 6 because according to the spec the lexeme should not wrap and it only matters that the token is in the 60 limit
        if ((writtenCount + 6) < 60 && t.getType() != TokenType.TUNDF)
        {
            // same line + append space
            // printToFile
            System.out.print(t.toString());
            printTokenToFile(t, false);

            // Incement
            writtenCount += charCount;
        }
        else
        {
            // new line 
            System.out.print(newlineChar);
            System.out.print(t.toString());
            // printToFile
            printTokenToFile(t, true);

            // Incement
            writtenCount = charCount;
        }

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

    private void printTokenToFile(Token token, boolean newline) throws IOException
    {
        try 
        {
            if (newline) 
            {
                tokenWriter.write(newlineChar);
                tokenWriter.write(token.toString());
            }
            else
            {
                tokenWriter.write(token.toString());
            }
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }
}
