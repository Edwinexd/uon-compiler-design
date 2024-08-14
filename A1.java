import java.io.IOException;
import java.nio.file.Paths;

public class A1 {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Source file name is missing");
        }

        TokenOutput tokenOutput = new TokenOutput();
        String fileName = Paths.get(args[0]).getFileName().toString();
        System.out.println(fileName);
        tokenOutput.initializeWriters(fileName + ".lst", fileName + "_tokens.txt");
        CD24Scanner scanner = new CD24Scanner(tokenOutput);
        scanner.scan(args[0]);
        tokenOutput.closeWriters();


    }
}
