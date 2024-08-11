// https://www.geeksforgeeks.org/symbol-table-compiler/


// Lexical Analysis: Creates new table entries in the table, for example like entries about tokens.
// Syntax Analysis: Adds information regarding attribute type, scope, dimension, line of reference, use, etc in the table.
// Semantic Analysis: Uses available information in the table to check for semantics i.e. to verify that expressions and assignments are semantically correct(type checking) and update it accordingly.


// Following operations can be performed on symbol table-

// 1. Insertion of an item in the symbol table.

// 2. Deletion of any item from the symbol table.

// 3. Searching of desired item from symbol table.



// Information used by the compiler from Symbol table:  

// Data type and name
// Declaring procedures
// Offset in storage
// If structure or record then, a pointer to structure table.
// For parameters, whether parameter passing by value or by reference
// Number and type of arguments passed to function
// Base Address

import java.util.HashMap;

public class SymbolTable 
{

    // key will be the name of the function = variable name

    // our key will probably be the name of the function and or variable.... if we see that this
    // clashes we can append other things to it 
    private HashMap<String, SymbolTableValue> map = new HashMap<String, SymbolTableValue>();

    // im assuming there will be an insert for each stage of the compile
    // so this method will be for lexical stage
    public void insertToken(String hash, Token token)
    {
        if (map.containsKey(hash))
        {
            SymbolTableValue current = map.get(hash);
            current.SetToken(token);
            map.put(hash, current);
        }
        else
        {
            map.put(hash, new SymbolTableValue(token));
        }

    }

    // syntax
    // public static void insertSyntax(String hash, things and stuff)
    // {
            // addd the same sort of tings like the above
            // if exists pull, edit the symboltablevalue then add back
    // }

    // Semantic
    // public static void insertSyntax(String hash, things and stuff)
    // {
            // addd the same sort of tings like the above
            // if exists pull, edit the symboltablevalue then add back
    // }



    // not sure if we will need specific getters for different methods later on but again, easy to add

    public Token getToken(String hash)
    {
        if (map.containsKey(hash))
        {
            return map.get(hash).GetToken();
        }
        return null;
    }

    public SymbolTableValue getSymbolTableValue(String hash)
    {
        if (map.containsKey(hash))
        {
            return map.get(hash);
        }
        return null;
    }

    // can add delete methods to delete specific things if we would like later
    // by just pulling the value editing then pushin... dont know if we will neeed that
    public void delete(String hash)
    {
        if (map.containsKey(hash))
        {
            map.remove(hash);
        }
    }


}
