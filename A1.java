import java.io.IOException;

public class A1 {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Source file name is missing");
        }

        // instead of static as later we may need to scan multiple files a symbol table 
        // will need to be instanciated for each new file when compiling. this is
        // because the keys are variable names and may be reused accross files.

        SymbolTable symbolTable = new SymbolTable();

        // when we merge afterwards we will need to add a symbol table into the scanner and then wherever we are 
        // adding variables / things and stuff that will have a variable name we can insert into the symbol table

        // to explain the thought better
        // method doThing()
        // int age
        // VARIABLE/METHOD/CONST/WHATEVER_NAME => "age" | doThing
        // symbolTable.insertToken(VARIABLE/METHOD/CONST/WHATEVER_NAME, token)
        TokenOutput tokenOutput = new TokenOutput();
        tokenOutput.initializeWriters("listingFile.txt", "tokenOutput.txt");
        CD24Scanner scanner = new CD24Scanner(tokenOutput);
        scanner.scan(args[0]);
        tokenOutput.closeWriters();


    }
}
