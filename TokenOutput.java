//import java.io.FileWriter;

public class TokenOutput {
    // resets when you make a newline
    private int writtenCount;

    public void write(Token t) 
    {

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
            System.out.println();
            // printToFile
            System.out.print(t);
            // Incement
            writtenCount = charCount;
            // will there be a space betwee?
        
        }

    }
}
