import java.io.FileWriter;

public class TokenOutput {
    // resets when you make a newline
    private int writtenCount;
    private FileWriter writer;

    /// i feel like the writer opening every time might cause too much overhead

    public void initializeWriter(String path)
    {
        try 
        {
            writer = new FileWriter(path);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }

    }

    public void closeWriter(String path)
    {
        try 
        {
            writer = new FileWriter(path);
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
            printToFile(output);

            // Incement
            writtenCount += charCount;
        }
        else
        {
            // new line + append space
            newLine();
            // printToFile
            printToFile(output);
            // Incement
            writtenCount += charCount;
            // will there be a space betwee?
        
        }

    }

    private void printToFile(String tokenName)
    {
        try 
        {
            writer.write(tokenName);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }

        System.out.print(tokenName);
    }

    private void newLine()
    {
        try 
        {
            writer.write("/n");
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }  

        System.out.println();
    }
}
