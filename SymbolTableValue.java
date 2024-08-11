// Data type and name
// Declaring procedures
// Offset in storage
// If structure or record then, a pointer to structure table.
// For parameters, whether parameter passing by value or by reference
// Number and type of arguments passed to function
// Base Address

public class SymbolTableValue {
    // lexical things and stuff
    private Token token;

    // other fields added later because i have no clue what we will need

    public SymbolTableValue()
    {

    }
    public SymbolTableValue(Token tokenIn)
    {
        token = tokenIn;
    }





    public Token GetToken()
    {
        return this.token;
    }

    public void SetToken(Token tokenin) 
    {
        token = tokenin;
    }
}
