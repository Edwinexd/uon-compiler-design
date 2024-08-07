import java.io.FileNotFoundException;

public class A1 {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Source file name is missing");
        }
        CD24Scanner scanner = new CD24Scanner(new TokenOutput());
        scanner.scan(args[0]);
        
    }
}
