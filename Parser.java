// <program> ::= CD24 <id> <globals> <funcs> <mainbody>
// <globals> ::= <consts> <types> <arrays>
// <consts> ::= constants <initlist> | ε
// <initlist> ::= <init> <initlisttail>
// <initlisttail> ::= , <init> <initslisttail> | ε
// <init> ::= <id> = <expr>

// <types> ::= typedef <typelist> | ε
// <typelist> ::= <type> <typelisttail>
// <typelisttail> ::= <type> <typelisttail> | ε

// <type> ::= <structid> def <fields> end
// <type> ::= <typeid> def array [ <expr> ] of <structid> end
// <fields> ::= <sdecl> <fieldstail>
// <fieldstail> ::= <sdecl> <fieldstail> | ε

// <arrays> ::= arraydef <arrdecls> | ε
// <arrdecls> ::= <arrdecl> <arrdeclstail>
// <arrdeclstail> ::= , <arrdecl> <arrdeclstail> | ε 
// <arrdecl> ::= <id> : <typeid>

// <funcs> ::= <funcPrime> 
// <funcPrime> ::= <func> <funcPrime> | ε
// <func> ::= func <id> ( <plist> ) : <rtype> <funcbody>
// <rtype> ::= <stype> | void
// <plist> ::= <params> | ε
// <params> ::= <param> <paramsPrime>
// <paramsPrime> ::= , <param> <paramsPrime> | ε
// <param> ::= <parammaybeconst> <id> : <paramtail>
// <parammaybeconst> ::= const | ε
// <paramtail> ::= <typeid> | <stypeOrStructid>
// <funcbody> ::= <locals> begin <stats> end
// <locals> ::= <dlist> | ε
// <dlist> ::= <decl> <dlistPrime>
// <dlistPrime> ::= , <dlist> | ε
// <decl> ::=  <id> : <decltail>
// <decltail> ::= <typeid> | <stypeOrStructid>


// <mainbody> ::= main <slist> begin <stats> end CD24 <id>
// <slist> ::= <sdecl> <slistPrime>
// <slistPrime> ::= , <sdecl> <slistPrime> | ε
// <sdecl> ::= <id> : <stypeOrStructid>
// <stypeOrStructid> ::= <stype> | <structid>

// <stype> ::= int | float | bool

// <stats> ::= <stat>; <statstail> | <strstat> <statstail>
// <statstail> ::= <stat>; <statstail> | <strstat> <statstail> | ε
// <strstat> ::= <forstat> | <ifstat> | <switchstat> | <dostat>
// <stat> ::= <repstat> | <iostat> | <returnstat> | <asgnstatorcallstat>
// <asgnstatorcallstat> ::= <id> <asgnstatorcallstattail>
// <asgnstatorcallstattail> ::= <vartail> | ( <callstattail>
// <forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
// <repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>
// <dostat> ::= do <stats> while ( <bool> ) end
// <asgnlist> ::= <alist> | ε
// <alist> ::=<asgnstat> <alisttail>
// <alisttail> ::= , <asgnstat> <alisttail> | ε

// <ifstat> ::= if ( <bool> ) <stats> <ifstattail> end
// <ifstattail> ::= else <stats> | elif (<bool>) <stats> | ε

// <switchstat> ::= switch ( <expr> ) begin <caselist> end
// <caselist> ::= case <expr> : <stats> break ; <caselist> | default : <stats>

// <asgnstat> ::= <var> <asgnop> <bool>
// <asgnop> :: == | += | -= | *= | /=

// <iostat> ::= input <vlist> | print <prlist> | printline <prlist>

// <callstat> ::= <id> ( <callstattail>
// <callstattail> ::= <elist> ) | )

// <returnstat> ::= return void | return <expr>

// <vlist> ::= <var> <vlisttail>
// <vlisttail> ::= , <vlisttail> | ε
// <var> ::= <id><vartail>
// <vartail> ::= [<expr>]<vartailtail> | ε
// <vartailtail> ::= . <id> | ε

// <elist> ::= <bool> <elisttail>
// <elisttail> ::= , <elist> | ε
// <bool> ::= not <bool> | <bool><logop> <rel> | <rel>
// <rel> ::= <expr> <reltail>
// <reltail> ::= <relop><expr> | ε
// <logop> ::= and | or | xor
// <relop> ::= == | != | > | <= | < | >=

// TODO: Expr & exprtail may be incorrect!
// <expr> ::= <term> <exprtail>
// <exprtail> ::= + <expr> | - <expr> | ε
// TODO: Term & termtail may be incorrect!
// <term> ::= <fact> <termtail>
// <termtail> ::= * <term> | / <term> | ε
// <fact> ::= <fact> ^ <exponent> | <exponent>
// <exponent> ::= <var> | <intlit> | <reallit> | <fncall> | true | false
// <exponent> ::= ( <bool> )

// <fncall> ::= <id> ( <fncalltail>
// <fncalltail> ::= <elist> ) | )


// <prlist> ::= <printitem> <prlisttail>
// <prlisttail> ::= , <prlist> | ε
// <printitem> ::= <expr> | <string>

// <id>, <structid>, <typeid> are all simply identifier tokens returned by the scanner.
// <intlit>, <reallit> and <string> are also special tokens returned by the scanner.

import java.util.Arrays;
import java.util.LinkedList;

// We are intentionally breaking naming conventions here to match the CD24 grammar
public class Parser 
{
    private SymbolTable rootSymbolTable = new SymbolTable();
    private SymbolTable currentSymbolTable = rootSymbolTable;
    public LinkedList<Token> tokenList = new LinkedList<Token>();  
    private TokenOutput tokenOutput;
    private boolean unrecoverable = false;

    public Parser(LinkedList<Token> list, TokenOutput tokenOutput) 
    {
        tokenList = list;
        this.tokenOutput = tokenOutput;
    }

    public void initParsing()
    {
        SyntaxTreeNode syntaxTree = programParse();
    }

    //#region <program> ::= CD24 <id> <globals> <funcs> <mainbody>
    private SyntaxTreeNode programParse()
    {
        if (tokenList.peek().getType() != TokenType.TCD24)
        {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","CD24", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCD24);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            // Critical error
        }
        tokenList.pop(); // CD24
        if (tokenList.peek().getType() != TokenType.TIDEN)
        {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier for CD24", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            // Critical error
        }
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.PROGRAM);
        SyntaxTreeNode rootNode = new SyntaxTreeNode(TreeNodeType.NPROG, idToken, record);

        // Global
        if (tokenList.peek().getType() == TokenType.TCONS)
        {
            SyntaxTreeNode globals = globals();
            rootNode.setFirstChild(globals);
            if (unrecoverable) { return null; }
        }
        // Functions
        if (tokenList.peek().getType() == TokenType.TFUNC)
        {
            SyntaxTreeNode funcs = funcs();
            rootNode.setSecondChild(funcs);
            if (unrecoverable) { return null; }
        }
        if (tokenList.peek().getType() != TokenType.TMAIN)
        {
            // Critical error, someone decided not to include a mandatory main function
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","main", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TMAIN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            //throw new RuntimeException("No main function found :(");
        }
        currentSymbolTable = record.getScope();
        SyntaxTreeNode mainBody = mainbody();
        currentSymbolTable = currentSymbolTable.getParent();
        rootNode.setThirdChild(mainBody);

        return rootNode;
    }
    //endregion

    //#region <globals> ::= <consts> <types> <arrays>
    private SyntaxTreeNode globals() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NGLOB);
        if (tokenList.peek().getType() == TokenType.TCONS) {
            SyntaxTreeNode constsNode = consts();
            node.setFirstChild(constsNode);
            if (unrecoverable) { return null; }
        }
        if (tokenList.peek().getType() == TokenType.TTYPD) {
            SyntaxTreeNode typesNode = types();
            node.setSecondChild(typesNode);
            if (unrecoverable) { return null; }
        }
        if (tokenList.peek().getType() == TokenType.TARRD) {
            SyntaxTreeNode arraysNode = arrays();
            node.setThirdChild(arraysNode);
            if (unrecoverable) { return null; }
        }
        return node;
    }
    //endregion

    //#region <consts> ::= constants <initlist> | ε
    private SyntaxTreeNode consts() {
        // this does not produce its own node and just returns the initlist node
        if (tokenList.peek().getType() != TokenType.TCONS) {
            // Should not really have been called
            // Critical error, this function should not have been called
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","constants", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCONS);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            //throw new RuntimeException("Critical error, this function should not have been called");
        }
        tokenList.pop(); // constants
        return initlist();
    }
    //endregion

    //#region <initlist> ::= <init> <initlisttail>
    private SyntaxTreeNode initlist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NILIST);
        node.setFirstChild(init());
        // for some reason they dont want us to use the second child when we have two possible children
        SyntaxTreeNode tail = initlisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <initlisttail> ::= , <init> <initslisttail> | ε
    private SyntaxTreeNode initlisttail() {

        if (tokenList.peek().getType() != TokenType.TCOMA) {
            // this is an epsilon production
            return null;
        }

        tokenList.pop(); // ,

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NILIST);

        node.setFirstChild(init());
        if (unrecoverable) { return null; }

        SyntaxTreeNode tail = initlisttail();

        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }
    //endregion

    //#region <init> ::= <id> = <expr>
    private SyntaxTreeNode init() {
        // pop the id token
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Should not really have been called
            //throw new RuntimeException("Critical error, this function should not have been called");
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        // TODO Check if declarationtype here should be constant or int/float/bool etc.
        record.setDeclaration(Declaration.CONSTANT);

        if (tokenList.peek().getType() != TokenType.TEQUL) {
            //throw new RuntimeException("Invalid syntax, expected =");
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","=", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TEQUL);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // =

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NINIT, idToken, record);

        node.setFirstChild(expr());
        if (unrecoverable) { return null; }

        return node;
    }
    //endregion

    //#region <types> ::= typedef <typelist> | ε
    private SyntaxTreeNode types() {
        // this does not produce its own node and just returns the typelist node
        if (tokenList.peek().getType() != TokenType.TTYPD) {
            // Critical error, this function should not have been called
            //throw new RuntimeException("Critical error, this function should not have been called");
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","typedef", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTYPD);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // typedef

        return typelist();
    }
    //endregion

    //#region <typelist> ::= <type> <typelisttail>
    private SyntaxTreeNode typelist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NTYPEL);
        node.setFirstChild(type());
        SyntaxTreeNode tail = typelisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //#endregion

    //#region <typelisttail> ::= <type> <typelisttail> | ε
    private SyntaxTreeNode typelisttail() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // this is an epsilon production
            return null;
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NTYPEL);
        node.setFirstChild(type());
        if (unrecoverable) { return null; }
        SyntaxTreeNode tail = typelisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //#endregion

    //#region <type> ::= N/A (choosing which type to parse based on lookahead)
    /*
     * More or less a "proxy" for calling the correct type function.
     */
    // TODO: This won't work since symbol table records are not set at this point!
    // <type> ::= <typeid> def array <typetype> | <structid> def <typestruct>
    private SyntaxTreeNode type() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token idToken = tokenList.pop();

        if (tokenList.peek().getType() != TokenType.TTDEF) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","def", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTDEF);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // def

        if (tokenList.peek().getType() == TokenType.TARAY) {
            tokenList.pop(); // array
            return typetype(idToken);
        } else {
            return typestruct(idToken);
        }
    }
    //#endregion

    //#region <type> ::= <fields> end
    private SyntaxTreeNode typestruct(Token idToken) {
        SyntaxTreeNode fieldsNode = fields();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.STRUCT_TYPE);

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NRTYPE, idToken, record);

        node.setFirstChild(fieldsNode);

        return node;
    }
    //endregion

    //#region <type> ::= [ <expr> ] of <structid> end
    private SyntaxTreeNode typetype(Token idToken) {
        if (tokenList.peek().getType() != TokenType.TLBRK) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","[", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLBRK);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // [

        SyntaxTreeNode exprNode = expr();
        
        if (tokenList.peek().getType() != TokenType.TRBRK) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","]", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRBRK);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // ]

        if (tokenList.peek().getType() != TokenType.TTTOF) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","of", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTTOF);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // of 

        if (tokenList.peek().getType() != TokenType.TIDEN) {
            popTillTokenType(TokenType.TIDEN);
        }
        
        Token structIdToken = tokenList.pop();

        // create record for new declaration
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.arrayOfType(currentSymbolTable.getOrCreateToken(structIdToken.getLexeme(), structIdToken)));

        if (tokenList.peek().getType() != TokenType.TTEND) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NATYPE, idToken, record);
        node.setFirstChild(exprNode);
        return node;
    }
    //endregion

    //#region <fields> ::= <sdecl> <fieldstail>
    private SyntaxTreeNode fields() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFLIST);
        node.setFirstChild(sdecl());
        if (unrecoverable) { return null; }
        SyntaxTreeNode tail = fieldstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <fieldstail> ::= <sdecl> <fieldstail> | ε
    private SyntaxTreeNode fieldstail() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            return null;
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFLIST);
        node.setFirstChild(sdecl());
        if (unrecoverable) { return null; }
        SyntaxTreeNode tail = fieldstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <sdecl> ::= <id> : <stypeOrStructid>
    private SyntaxTreeNode sdecl() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token idToken = tokenList.pop();
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCOLN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // :
        TreeNodeType outType;
        Declaration typeDeclaration = stypeOrStructid();
        if (unrecoverable) { return null; }
        if (typeDeclaration.isPrimitive()) {
            // NSDECL
            outType = TreeNodeType.NSDECL;
        } else {
            // NTDECL
            outType = TreeNodeType.NTDECL;
        }
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(typeDeclaration);
        SyntaxTreeNode node = new SyntaxTreeNode(outType, idToken, record);
        return node;
    }
    //endregion

    //#region <arrays> ::= arraydef <arrdecls> | ε
    private SyntaxTreeNode arrays() {
        // Note: this will not have been called if the next token is not arraydef so should prob be removed
        if (tokenList.peek().getType() != TokenType.TARRD) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","arraydef", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TARRD);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // arraydef
        return arrdecls();
    }
    //endregion

    //#region <arrdecls> ::= <arrdecl> <arrdeclstail>
    private SyntaxTreeNode arrdecls() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NALIST);
        node.setFirstChild(arrdecl());
        SyntaxTreeNode tail = arrdeclstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <arrdeclstail> ::= , <arrdecl> <arrdeclstail> | ε 
    private SyntaxTreeNode arrdeclstail() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            return null;
        }
        tokenList.pop(); // ,
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NALIST);
        node.setFirstChild(arrdecl());
        SyntaxTreeNode tail = arrdeclstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <arrdecl> ::= <id> : <typeid>
    private SyntaxTreeNode arrdecl() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token idToken = tokenList.pop();
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCOLN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // :
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token typeIdToke = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        SymbolTableRecord typeRecord = currentSymbolTable.getOrCreateToken(typeIdToke.getLexeme(), typeIdToke);
        if (typeRecord.getDeclaration().isPresent() && !record.getDeclaration().get().equals(Declaration.ARRAY_TYPE)) {
            // TODO Incorrect type
            throw new RuntimeException("Critical error, expected an array type");
        }
        record.setDeclaration(Declaration.arrayOfType(typeRecord));
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NARRD, idToken, record);
        return node;
    }
    //endregion

    //#region <funcs> ::= <funcPrime> 
    private SyntaxTreeNode funcs() {
        return funcsPrime();
    }
    //endregion

    //#region <funcPrime> ::= <func> <funcPrime> | ε
    private SyntaxTreeNode funcsPrime() {
        if (tokenList.peek().getType() != TokenType.TFUNC) {
            return null;
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFUNCS);
        node.setFirstChild(func());
        SyntaxTreeNode tail = funcsPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <func> ::= func <id> ( <plist> ) : <rtype> <funcbody>
    private SyntaxTreeNode func() {
        if (tokenList.peek().getType() != TokenType.TFUNC) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","func", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TFUNC);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // func
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.FUNCTION);
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // (
        // TODO: Should be part of func parameters + scope
        SyntaxTreeNode plistNode = plist();
        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // )
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCOLN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // :
        record.setReturnType(rtype());
        if (unrecoverable) { return null; }
        // Enter the function's scope
        currentSymbolTable = record.getScope();
        SyntaxTreeNode[] body = funcbody();
        SyntaxTreeNode locals = body[0];
        SyntaxTreeNode stats = body[1];
        // Exit the function's scope
        currentSymbolTable = currentSymbolTable.getParent();

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFUND, idToken, record);
        node.setFirstChild(plistNode);
        node.setSecondChild(locals);
        node.setThirdChild(stats);
        return node;
    }
    //endregion

    //#region <rtype> ::= <stype> | void
    private Declaration rtype() {
        if (tokenList.peek().getType() == TokenType.TVOID) {
            tokenList.pop();
            return Declaration.VOID;
        }
        return stype();
    }
    //endregion

    //#region <plist> ::= <params> | ε
    private SyntaxTreeNode plist() {
        if (tokenList.peek().getType() != TokenType.TIDEN && tokenList.peek().getType() != TokenType.TCONS) {
            return null;
        }
        return params();
    }
    //endregion
    
    //#region <params> ::= <param> <paramsPrime>
    private SyntaxTreeNode params() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NPLIST);
        node.setFirstChild(param());

        SyntaxTreeNode tail = paramsPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }
    //endregion

    //#region <paramsPrime> ::= , <param> <paramsPrime> | ε
    private SyntaxTreeNode paramsPrime() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            return null;
        }
        tokenList.pop(); // ,
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NPLIST);
        node.setFirstChild(param());
        SyntaxTreeNode tail = paramsPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <param> ::= <parammaybeconst> <id> : <paramtail>
    private SyntaxTreeNode param() {
        boolean isConst = parammaybeconst();
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token idToken = tokenList.pop();
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCOLN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // :
        Declaration type = paramtail(isConst);
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(type);
        if (type.equals(Declaration.STRUCT_TYPE) || type.isPrimitive()) {
            // NSIMP
            return new SyntaxTreeNode(TreeNodeType.NSIMP, idToken, record);
        } else if (type.getType() == DeclarationType.ARRAY) {
            // NARRP
            return new SyntaxTreeNode(TreeNodeType.NARRP, idToken, record);
        } else if (type.getType() == DeclarationType.ARRAY_CONSTANT) {
            // NARRC
            return new SyntaxTreeNode(TreeNodeType.NARRC, idToken, record);
        }
        // TODO: This should be unreachable?
        throw new RuntimeException("Critical error, expected stype or array type");
    }
    //endregion

    //#region <parammaybeconst> ::= const | ε
    private boolean parammaybeconst() {
        if (tokenList.peek().getType() != TokenType.TCONS) {
            return false;
        }
        tokenList.pop(); // const
        return true;
    }
    //endregion

    //#region <paramtail> ::= <typeid> | <stypeOrStructid>
    private Declaration paramtail(boolean isConst) {
        // TODO: This is wrong since stypeOrStructid could be ok...
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            popTillTokenType(TokenType.TIDEN);
        }
        // TODO: This wont work either
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (record.getDeclaration().isPresent() && record.getDeclaration().get().equals(Declaration.ARRAY_TYPE)) {
            if (isConst) {
                return Declaration.arrayConstantOfType(record);
            }
            return Declaration.arrayOfType(record);
        }
        return stypeOrStructid();
    }
    //endregion

    //#region <funcbody> ::= <locals> begin <stats> end
    private SyntaxTreeNode[] funcbody() {
        SyntaxTreeNode[] nodes = new SyntaxTreeNode[2];
        nodes[0] = locals();
        if (tokenList.peek().getType() != TokenType.TBEGN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","begin", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TBEGN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // begin
        nodes[1] = stats();
        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // end
    }
    //endregion

    //#region <locals> ::= <dlist> | ε
    private SyntaxTreeNode locals() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            return null;
        }
        return dlist();
    }
    //endregion
    
    //#region <dlist> ::= <decl> <dlistPrime>
    private SyntaxTreeNode dlist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NDLIST);
        node.setFirstChild(decl());
        SyntaxTreeNode tail = dlistPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    //endregion

    //#region <dlistPrime> ::= , <dlist> | ε
    private SyntaxTreeNode dlistPrime() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            return null;
        }
        tokenList.pop(); // ,
        return dlist();
    }
    //endregion

    //#region <decl> ::=  <id> : <decltail>
    private SyntaxTreeNode decl() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        Token idToken = tokenList.pop();

        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCOLN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // :

        Declaration type = decltail();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(type);
        if (type.getType() == DeclarationType.STRUCT) {
            // NTDECL
            return new SyntaxTreeNode(TreeNodeType.NTDECL, idToken, record);
        } else if (type.isPrimitive()) {
            // NSDECL
            return new SyntaxTreeNode(TreeNodeType.NSDECL, idToken, record);
        } else if (type.getType() == DeclarationType.ARRAY) {
            // NARRD
            return new SyntaxTreeNode(TreeNodeType.NARRD, idToken, record);
        }

        // TODO: This should be unreachable?
        throw new RuntimeException("Critical error, expected stype or array type");
    }
    //endregion

    //#region <decltail> ::= <typeid> | <stypeOrStructid>
    private Declaration decltail() {
        return paramtail(false); // Same as paramtail but not const
    }
    //endregion

    //#region <mainbody> ::= main <slist> begin <stats> end CD24 <id>
    private SyntaxTreeNode mainbody() {
        if (tokenList.peek().getType() != TokenType.TMAIN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","main", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TMAIN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // main

        SyntaxTreeNode slistNode = slist();

        if (tokenList.peek().getType() != TokenType.TBEGN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","begin", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TBEGN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // begin

        SyntaxTreeNode statsNode = stats();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end

        if (tokenList.peek().getType() != TokenType.TCD24) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","CD24", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCD24);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // CD24

        Token idToken = tokenList.pop();

        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (record.getDeclaration().isEmpty() || !record.getDeclaration().get().equals(Declaration.PROGRAM)) {
            // TODO End of main id should be the same as the program id
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NMAIN, idToken, record);
        node.setFirstChild(slistNode);
        node.setSecondChild(statsNode);
        return node;
    }
    //endregion
    
    //#region <slist> ::= <sdecl> <slistPrime>
    private SyntaxTreeNode slist() {
        sdecl();
        if (unrecoverable) { return null; }
        slistPrime();
        if (unrecoverable) { return null; }
    }
    //endregion

    //#region <slistPrime> ::= , <sdecl> <slistPrime> | ε
    private SyntaxTreeNode slistPrime() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            sdecl();
            if (unrecoverable) { return null; }
            slistPrime();
            if (unrecoverable) { return null; }
        }

        //// eeeeeee bruv

    }
    //endregion

    // <stypeOrStructid> ::= <stype> | <structid>
    private Declaration stypeOrStructid() {
        Token peekedToken = tokenList.peek();
        TokenType peekedType = peekedToken.getType();
        if (peekedType == TokenType.TINTG) {
            tokenList.pop();
            return Declaration.INT;
        } else if (peekedType == TokenType.TFLOT) {
            tokenList.pop();
            return Declaration.FLOAT;
        } else if (peekedType == TokenType.TBOOL) {
            tokenList.pop();
            return Declaration.BOOL;
        }
        if (peekedType != TokenType.TIDEN) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Struct Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            //throw new RuntimeException("Critical error, expected a struct identifier or a type");
        }
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (record.getDeclaration().isPresent() && !record.getDeclaration().get().equals(Declaration.STRUCT_TYPE)) {
            // TODO Incorrect type
            //         unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TINTG, TokenType.TFLOT, TokenType.TBOOL, TokenType.TIDEN)));
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Struct Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            //throw new RuntimeException("Critical error, expected a struct identifier");
        }
        return Declaration.structOfType(record);
        
    }
    //endregion

    // <stype> ::= int | float | bool
    private Declaration stype() {
        // TODO: Pop until
        Token peekedToken = tokenList.peek();
        TokenType peekedType = peekedToken.getType();
        if (peekedType == TokenType.TINTG) {
            tokenList.pop();
            return Declaration.INT;
        } else if (peekedType == TokenType.TFLOT) {
            tokenList.pop();
            return Declaration.FLOAT;
        } else if (peekedType == TokenType.TBOOL) {
            tokenList.pop();
            return Declaration.BOOL;
        }
        throw new RuntimeException("Critical error, expected a type");
    }



    // <stats> ::= <stat>; <statstail> | <strstat> <statstail>
    private SyntaxTreeNode stats() {
        return statstail(true);
    }


    // <statstail> ::= <stat>; <statstail> | <strstat> <statstail> | ε
    private SyntaxTreeNode statstail(boolean forbidEpsilon) {
        Token peekedToken = tokenList.peek();
        TokenType peekedType = peekedToken.getType();
        // strstat:
        boolean strstatOrstat = (
            peekedType == TokenType.TTFOR ||
            peekedType == TokenType.TIFTH ||
            peekedType == TokenType.TSWTH ||
            peekedType == TokenType.TTTDO ||
            // and stat:
            peekedType == TokenType.TREPT ||
            peekedType == TokenType.TIDEN ||
            peekedType == TokenType.TINPT ||
            peekedType == TokenType.TPRNT ||
            peekedType == TokenType.TPRLN ||
            peekedType == TokenType.TRETN
        );
        if (strstatOrstat && forbidEpsilon) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Statement", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TTFOR, TokenType.TIFTH, TokenType.TSWTH, TokenType.TTTDO, TokenType.TREPT, TokenType.TIDEN, TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN, TokenType.TRETN)));
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        
        if (!strstatOrstat && !forbidEpsilon) {
            return null;
        }

        
        SyntaxTreeNode firstNode;
        // stat
        if (peekedType == TokenType.TREPT || 
            peekedType == TokenType.TIDEN || 
            peekedType == TokenType.TINPT || 
            peekedType == TokenType.TPRNT || 
            peekedType == TokenType.TPRLN || 
            peekedType == TokenType.TRETN)
        {
            firstNode = stat();

            if (tokenList.peek().getType() != TokenType.TSEMI) {
                // Critical error
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",";", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TSEMI);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }

            tokenList.pop(); // ;
        } else {
            firstNode = strstat();
        }
        // strstat
        // (ttfor | tifth | tswth | tttdo)

        SyntaxTreeNode tail = statstail(false);

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NSTATS);

        node.setFirstChild(firstNode);

        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }


    // <strstat> ::= <forstat> | <ifstat> | <switchstat> | <dostat>
    private SyntaxTreeNode strstat() {
        while (true) {
            if (tokenList.peek().getType() == TokenType.TTFOR) {
                return forstat();
            }
            if (tokenList.peek().getType() == TokenType.TIFTH) {
                return ifstat();
            }
            if (tokenList.peek().getType() == TokenType.TSWTH) {
                return switchstat();
            }
            if (tokenList.peek().getType() == TokenType.TTTDO) {
                return dostat();
            }
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Statement", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TTFOR, TokenType.TIFTH, TokenType.TSWTH, TokenType.TTTDO)));
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
    }


    // <stat> ::= <repstat> | <iostat> | <returnstat> | <asgnstatorcallstat>
    private SyntaxTreeNode stat() {
        while (true) {
            if (tokenList.peek().getType() == TokenType.TREPT) {

                return repstat();
            }
            if (tokenList.peek().getType() == TokenType.TINPT || 
                tokenList.peek().getType() == TokenType.TPRNT || 
                tokenList.peek().getType() == TokenType.TPRLN) {

                return iostat();
            }
            if (tokenList.peek().getType() == TokenType.TRETN) {
                return returnstat();

            }
            if (tokenList.peek().getType() == TokenType.TIDEN) {
                return asgnstatorcallstat();
            }
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Statement", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TREPT, TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN, TokenType.TRETN, TokenType.TIDEN)));
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
    }
    // <asgnstatorcallstat> ::= <id> <asgnstatorcallstattail>
    private SyntaxTreeNode asgnstatorcallstat() {

        if (tokenList.peek().getType() != TokenType.TIDEN) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        } 

        Token idToken = tokenList.pop();

        return asgnstatorcallstattail(idToken);
    }
    // <asgnstatorcallstattail> ::= <vartail> | ( <callstattail>
    private SyntaxTreeNode asgnstatorcallstattail(Token idToken) {
        while (true) {
            TokenType currentType = tokenList.peek().getType();

            if (currentType == TokenType.TLBRK) {
                return vartail(idToken);
            }
            if (currentType == TokenType.TLPAR) {
                tokenList.pop(); // (

                // Same implementation as the entire callstat function

                SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
                SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NCALL, idToken, record);

                SyntaxTreeNode elistNode = callstattail();
                if (elistNode != null) {
                    node.setFirstChild(elistNode);
                }
            
                return node;
            }
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","[ or (", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TLBRK, TokenType.TLPAR)));
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
    }

    // <forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
    private SyntaxTreeNode forstat() {
        if (tokenList.peek().getType() != TokenType.TTFOR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","for", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTFOR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        
        tokenList.pop(); // for

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        SyntaxTreeNode asgnlistNode = asgnlist();

        if (tokenList.peek().getType() != TokenType.TSEMI) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",";", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TSEMI);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // ;

        SyntaxTreeNode boolNode = boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        SyntaxTreeNode statsNode = stats();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end

        SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NFORL);
        result.setFirstChild(asgnlistNode);
        result.setSecondChild(boolNode);
        result.setThirdChild(statsNode);
        return result;
    }

    // <repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>
    private SyntaxTreeNode repstat() {
        if (tokenList.peek().getType() != TokenType.TREPT) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","repeat", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TREPT);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // repeat

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        SyntaxTreeNode asgnlistNode = asgnlist();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        SyntaxTreeNode statsNode = stats();

        if (tokenList.peek().getType() != TokenType.TUNTL) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","until", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TUNTL);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // until

        SyntaxTreeNode boolNode = boolParse();

        SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NREPT);
        result.setFirstChild(asgnlistNode);
        result.setSecondChild(statsNode);
        result.setThirdChild(boolNode);
        return result;
    }
    // <dostat> ::= do <stats> while ( <bool> ) end
    private SyntaxTreeNode dostat() {
        if (tokenList.peek().getType() != TokenType.TTTDO) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","do", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTTDO);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // do

        SyntaxTreeNode statsNode = stats();

        if (tokenList.peek().getType() != TokenType.TWHIL) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","while", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TWHIL);
            // Critical error
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // while

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (


        SyntaxTreeNode boolNode = boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        if (tokenList.peek().getType() != TokenType.TTEND) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end

        SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NDOWL);
        result.setFirstChild(statsNode);
        result.setThirdChild(boolNode);

        return result;
    }
    // <asgnlist> ::= <alist> | ε
    private SyntaxTreeNode asgnlist() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            return null;
        }
        return alist();
    }

    // <alist> ::= <asgnstat> <alisttail>
    private SyntaxTreeNode alist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NASGNS);
        node.setFirstChild(asgnstat());

        SyntaxTreeNode tail = alisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }

    // <alisttail> ::= , <asgnstat> <alisttail> | ε
    private SyntaxTreeNode alisttail() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            return null;
        }
        tokenList.pop(); // ,
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NASGNS);
        node.setFirstChild(asgnstat());

        SyntaxTreeNode tail = alisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }

    // <ifstat> ::= if ( <bool> ) <stats> <ifstattail> end
    private SyntaxTreeNode ifstat() {
        if (tokenList.peek().getType() != TokenType.TIFTH) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","if", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIFTH);
            // Critical error
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // if

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            // Critical error
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        SyntaxTreeNode boolNode = boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            // Critical error
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        SyntaxTreeNode statsNode = stats();

        SyntaxTreeNode tail = ifstattail();
        // tail can either be null, stats (else) or an ifstat (elif)

        if (tokenList.peek().getType() != TokenType.TTEND) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            // Critical error
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end

        TreeNodeType resultNodeType = tail == null ? TreeNodeType.NIFTH : TreeNodeType.NIFTE;

        SyntaxTreeNode node = new SyntaxTreeNode(resultNodeType);
        node.setFirstChild(boolNode);
        node.setSecondChild(tail);
        node.setThirdChild(statsNode);

        return node;
    }

    // <ifstattail> ::= else <stats> | elif (<bool>) <stats> | ε
    private SyntaxTreeNode ifstattail() {
        if (tokenList.peek().getType() == TokenType.TELSE) {
            
            tokenList.pop(); // else

            return stats();
        }
        if (tokenList.peek().getType() == TokenType.TELIF) {
            tokenList.pop(); // elif

            if (tokenList.peek().getType() != TokenType.TLPAR) {
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TLPAR);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }

            tokenList.pop(); // (

            SyntaxTreeNode boolNode = boolParse();

            if (tokenList.peek().getType() != TokenType.TRPAR) {
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TRPAR);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }

            tokenList.pop(); // )

            SyntaxTreeNode statsNode = stats();

            SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NIFTH);
            result.setFirstChild(boolNode);
            result.setThirdChild(statsNode);

            return result;
        }
        // ε 
    }

    // <switchstat> ::= switch ( <expr> ) begin <caselist> end
    private SyntaxTreeNode switchstat() {
        if (tokenList.peek().getType() != TokenType.TSWTH) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","switch", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TSWTH);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // switch
            
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        SyntaxTreeNode exprNode = expr();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        if (tokenList.peek().getType() != TokenType.TBEGN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","begin", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TBEGN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // begin

        SyntaxTreeNode caselistNode = caselist();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","end", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TTEND);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // end
    }
    // <caselist> ::= case <expr> : <stats> break ; <caselist> | default : <stats>
    private SyntaxTreeNode caselist() {
        Token peekedToken = tokenList.peek();
        TokenType peekedType = peekedToken.getType();
        if (peekedType != TokenType.TCASE && peekedType != TokenType.TDFLT) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","case or default", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TCASE, TokenType.TDFLT)));
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        if (tokenList.peek().getType() == TokenType.TCASE) {

            tokenList.pop(); // case

            SyntaxTreeNode exprNode = expr();

            if (tokenList.peek().getType() != TokenType.TCOLN) {
                // Critical error
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TCOLN);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }
            
            tokenList.pop(); // :

            SyntaxTreeNode statsNode = stats();

            if (tokenList.peek().getType() != TokenType.TBREK) {
                // Critical error
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","break", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TBREK);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }

            tokenList.pop(); // break

            if (tokenList.peek().getType() != TokenType.TSEMI) {
                // Critical error
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",";", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TSEMI);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }

            tokenList.pop(); // ;

            SyntaxTreeNode caselistNode = caselist();

            SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NCASLT);
            node.setFirstChild(exprNode);
            node.setSecondChild(statsNode);
            node.setThirdChild(caselistNode);
            return node;

        }
        // default
        tokenList.pop(); // default

        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",":", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TCOLN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // :

        SyntaxTreeNode statsNode = stats();

        // TODO: Not sure if this is the intended structure for default case
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NCASLT);
        node.setSecondChild(statsNode);
        return node;
    }

    // <asgnstat> ::= <var> <asgnop> <bool>
    private SyntaxTreeNode asgnstat() {
        SyntaxTreeNode varNode = var();
        TreeNodeType asgnopNode;
        try {
            asgnopNode = asgnop();
        } catch (RuntimeException e) {
            return new SyntaxTreeNode(TreeNodeType.NUNDEF);
        }
        SyntaxTreeNode boolNode = boolParse();

        SyntaxTreeNode node = new SyntaxTreeNode(asgnopNode);

        node.setFirstChild(varNode);
        node.setThirdChild(boolNode);

        return node;
    }
    // <asgnop> :: == | += | -= | *= | /=
    private TreeNodeType asgnop() {
        while (true) {
            if (tokenList.peek().getType() == TokenType.TEQUL) {
                tokenList.pop();
                return TreeNodeType.NASGN;
            } else if (tokenList.peek().getType() == TokenType.TPLEQ) {
                tokenList.pop();
                return TreeNodeType.NPLEQ;
            } else if (tokenList.peek().getType() == TokenType.TMNEQ) {
                tokenList.pop();
                return TreeNodeType.NMNEQ;
            } else if (tokenList.peek().getType() == TokenType.TSTEQ) {
                tokenList.pop();
                return TreeNodeType.NSTEA;
            } else if (tokenList.peek().getType() == TokenType.TDVEQ) {
                tokenList.pop();
                return TreeNodeType.NDVEQ;
            }
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Assignment Operator", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TEQUL, TokenType.TPLEQ, TokenType.TMNEQ, TokenType.TSTEQ, TokenType.TDVEQ)));
            if (unrecoverable) { throw new RuntimeException("Unrecoverable error"); }
        }
    }

    // <iostat> ::= input <vlist> | print <prlist> | printline <prlist>
    private SyntaxTreeNode iostat() {
        Token peekedToken = tokenList.peek();
        if (!(peekedToken.getType() == TokenType.TINPT || 
            peekedToken.getType() == TokenType.TPRNT || 
            peekedToken.getType() == TokenType.TPRLN)) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Input, Print, or Printline", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN)));
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // input, print, or printline
        if (peekedToken.getType() == TokenType.TINPT) {
            SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NINPUT);
            SyntaxTreeNode vlistNode = vlist();
            node.setFirstChild(vlistNode);
            return node;
        }

        SyntaxTreeNode node = new SyntaxTreeNode(peekedToken.getType() == TokenType.TPRNT ? TreeNodeType.NPRINT : TreeNodeType.NPRLN);

        SyntaxTreeNode prlistNode = prlist();

        node.setFirstChild(prlistNode);

        return node;
    }

    // <callstat> ::= <id> ( <callstattail>
    private SyntaxTreeNode callstat() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        Token idToken = tokenList.pop();

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NCALL, idToken, record);
        
        SyntaxTreeNode elistNode = callstattail();
        if (elistNode != null) {
            node.setFirstChild(elistNode);
        }

        return node;
    }

    // <callstattail> ::= <elist> ) | )
    private SyntaxTreeNode callstattail() {

        if (tokenList.peek().getType() == TokenType.TRPAR) {

            tokenList.pop(); // )
            return null;
        }

        SyntaxTreeNode elistNode = elist();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        return elistNode;
    }

    // Note: This is easier to implement as a single function for both production rules
    // <returnstat> ::= return <returnstattail>
    // <returnstattail> ::= void | <expr>
    private SyntaxTreeNode returnstat() {
        if (tokenList.peek().getType() != TokenType.TRETN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","return", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRETN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        tokenList.pop(); // return

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NRETN);
        if (tokenList.peek().getType() == TokenType.TVOID) {
            tokenList.pop(); // void
        } else {
            node.setFirstChild(expr());
        }
        return node;
    }
    // <vlist> ::= <var> <vlisttail>
    private SyntaxTreeNode vlist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NVLIST);
        node.setFirstChild(var());
        SyntaxTreeNode tail = vlisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // <vlisttail> ::= , <vlisttail> | ε
    private SyntaxTreeNode vlisttail() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            return null;
        }
        tokenList.pop(); // ,
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NVLIST);
        node.setFirstChild(var());
        SyntaxTreeNode tail = vlisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // <var> ::= <id><vartail>
    private SyntaxTreeNode var() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }
        Token idToken = tokenList.pop();
        return vartail(idToken);
    }
    // <vartail> ::= [<expr>]<vartailtail> | ε
    private SyntaxTreeNode vartail(Token idToken) {
        if (tokenList.peek().getType() == TokenType.TLBRK) {
            tokenList.pop(); // [

            SyntaxTreeNode exprNode = expr();

            if (tokenList.peek().getType() != TokenType.TRBRK) {
                // Critical error
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","]", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TRBRK);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }

            tokenList.pop(); // ]

            return vartailtail(idToken, exprNode);
        }
        // Epsilon production
        // TODO: This should use getSymbol - not create as that would be a syntax error!
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        return new SyntaxTreeNode(TreeNodeType.NSIMV, idToken, record);
    }
    // <vartailtail> ::= . <id> | ε
    private SyntaxTreeNode vartailtail(Token idToken, SyntaxTreeNode exprNode) {
        if (tokenList.peek().getType() == TokenType.TDOTT) {

            tokenList.pop(); // .

            if (tokenList.peek().getType() != TokenType.TIDEN) {
                // Critical error
                tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
                unrecoverable = popTillTokenType(TokenType.TIDEN);
                if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
            }
            Token endIdToken = tokenList.pop();

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
            currentSymbolTable = record.getScope();
            SymbolTableRecord endRecord = currentSymbolTable.getOrCreateToken(endIdToken.getLexeme(), endIdToken);
            currentSymbolTable = currentSymbolTable.getParent();

            // TODO: Not completely sure what children of this node should be? We technically have two ids?
            SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NARRV, idToken, record);
            node.setFirstChild(exprNode);
            node.setThirdChild(new SyntaxTreeNode(TreeNodeType.NSIMV, endIdToken, endRecord));
            return node;
        }
        // Epsilon production
        // TODO: This should use getSymbol - not create as that would be a syntax error!
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NAELT, idToken, record);
        node.setFirstChild(exprNode);
        return node;
    }
    // <elist> ::= <bool> <elisttail>
    private void elist() {
        boolParse();
        if (unrecoverable) { return null; }
        elisttail();
        if (unrecoverable) { return null; }
    }
    // <elisttail> ::= , <elist> | ε
    private void elisttail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {

            tokenList.pop(); // ,

            elist();
            if (unrecoverable) { return null; }

        } else {
            // Eee
        }
    }
    // <bool> ::= not <bool> | <bool><logop> <rel> | <rel>
    private void boolParse() {
        if (tokenList.peek().getType() == TokenType.TNOTT) {

            tokenList.pop(); // not

            boolParse();
            if (unrecoverable) { return null; }

        } else {

            rel();
            if (unrecoverable) { return null; }

            if (tokenList.peek().getType() == TokenType.TTAND || 
                tokenList.peek().getType() == TokenType.TTTOR || 
                tokenList.peek().getType() == TokenType.TTXOR) {

                tokenList.pop(); // logop

                rel();
                if (unrecoverable) { return null; }

            }

        }
    }
    // <rel> ::= <expr> <reltail>
    private void rel() {
        expr();
        reltail();
    }
    // <reltail> ::= <relop><expr> | ε
    private void reltail() {
        if (tokenList.peek().getType() == TokenType.TEQEQ || 
            tokenList.peek().getType() == TokenType.TNEQL || 
            tokenList.peek().getType() == TokenType.TGRTR || 
            tokenList.peek().getType() == TokenType.TLEQL || 
            tokenList.peek().getType() == TokenType.TLESS || 
            tokenList.peek().getType() == TokenType.TGEQL) {

            relop();
            if (unrecoverable) { return null; }

            expr();
            if (unrecoverable) { return null; }

        } else {
            // Eeeeee ee eee eee eee
        }
    }
    // <logop> ::= and | or | xor
    private void logop() {
        if (tokenList.peek().getType() == TokenType.TTAND) {

            tokenList.pop(); // and

            return;
        } else if (tokenList.peek().getType() == TokenType.TTTOR) {

            tokenList.pop(); // or

            return;
        } else if (tokenList.peek().getType() == TokenType.TTXOR) {

            tokenList.pop(); // xor

            return;
        }

        // Critical error
        tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","and, or, or xor", tokenList.peek().getLine(), tokenList.peek().getColumn()));
        unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TTAND, TokenType.TTTOR, TokenType.TTXOR)));
        if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        logop(); // recall itself
        if (unrecoverable) { return null; }
    }
    // <relop> ::= == | != | > | <= | < | >=
    private void relop() {
        if (tokenList.peek().getType() == TokenType.TEQEQ) {

            // treeeeeeeee

            tokenList.pop(); // ==

            return;
        } else if (tokenList.peek().getType() == TokenType.TNEQL) {

            // treeeeeeeee

            tokenList.pop(); // !=

            return;
        } else if (tokenList.peek().getType() == TokenType.TGRTR) {

            // treeeeeeeee

            tokenList.pop(); // >

            return;
        } else if (tokenList.peek().getType() == TokenType.TLEQL) {

            // treeeeeeeee

            tokenList.pop(); // <=

            return;
        } else if (tokenList.peek().getType() == TokenType.TLESS) {

            // treeeeeeeee

            tokenList.pop(); // <

            return;
        } else if (tokenList.peek().getType() == TokenType.TGEQL) {

            // treeeeeeeee

            tokenList.pop(); // >=

            return;
        }
        // Critical error
        tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","==, !=, >, <=, <, or >=", tokenList.peek().getLine(), tokenList.peek().getColumn()));
        unrecoverable = popTillTokenType(new LinkedList<TokenType>(Arrays.asList(TokenType.TEQEQ, TokenType.TNEQL, TokenType.TGRTR, TokenType.TLEQL, TokenType.TLESS, TokenType.TGEQL)));
        if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        relop();    // recall itself
        if (unrecoverable) { return null; }
    }

    // TODO: Expr & exprtail may be incorrect!
    // <expr> ::= <term> <exprtail>
    private void expr() {
        term();
        if (unrecoverable) { return null; }
        exprtail();
        if (unrecoverable) { return null; }
    }
    // <exprtail> ::= + <expr> | - <expr> | ε
    private void exprtail() {
        if (tokenList.peek().getType() == TokenType.TPLUS || 
            tokenList.peek().getType() == TokenType.TMINS) {

            tokenList.pop(); // + or -

            expr();
            if (unrecoverable) { return null; }

        } else {
            // Eee
        }
    }
    // TODO: Term & termtail may be incorrect!
    // <term> ::= <fact> <termtail>
    private void term() {
        fact();
        if (unrecoverable) { return null; }
        termtail();
        if (unrecoverable) { return null; }
    }

    // <termtail> ::= * <term> | / <term> | ε
    private void termtail() {
        if (tokenList.peek().getType() == TokenType.TSTAR || 
            tokenList.peek().getType() == TokenType.TDIVD) {

            tokenList.pop(); // * or /

            term();
            if (unrecoverable) { return null; }

        } else {
            // Eee
        }
    }

    // <fact> ::= <exponent> <factPrime>
    private void fact() {
        exponent();
        if (unrecoverable) { return null; }
        factPrime();
        if (unrecoverable) { return null; }
    }

    // <factPrime> ::= ^ <exponent> <factPrime> | ε
    private void factPrime() {
        if (tokenList.peek().getType() == TokenType.TCART) {

            tokenList.pop(); // ^

            exponent();
            if (unrecoverable) { return null; }

            factPrime();
            if (unrecoverable) { return null; }

        } else {
            // Eee
        }
    }

     // <exponent> ::= <exponentNotBool> | <exponentBool>
     private void exponent() {
        if (tokenList.peek().getType() == TokenType.TLPAR) {

            exponentBool();
            if (unrecoverable) { return null; }

        } else {

            exponentNotBool();
            if (unrecoverable) { return null; }
            
        }
    }

    // <exponentNotBool> ::= <var> | <intlit> | <reallit> | <fncall> | true | false
    private void exponentNotBool() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {

            var();
            if (unrecoverable) { return null; }

        } else if (tokenList.peek().getType() == TokenType.TILIT) {

            tokenList.pop(); // intlit (int)

        } else if (tokenList.peek().getType() == TokenType.TFLIT) {

            tokenList.pop(); // reallit (float)

        } else if (tokenList.peek().getType() == TokenType.TTRUE || 
                   tokenList.peek().getType() == TokenType.TFALS) {

            tokenList.pop(); // true or false

        } else {

            fncall();
            if (unrecoverable) { return null; }

        }
    }

    // <exponentBool> ::= ( <bool> )
    public void exponentBool() {
            
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        boolParse();
        if (unrecoverable) { return null; }

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

    }


    // <fncall> ::= <id> ( <fncalltail>
    private void fncall() {
        ïf (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TIDEN);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        Token idToken = tokenList.pop();

        Record record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);


        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ","(", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TLPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // (

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFCALL, idToken, record);

        SyntaxTreeNode elistNode = fncalltail();
        if (elistNode != null) {
            node.setFirstChild(elistNode);
        }

        return node;

    }

    // <fncalltail> ::= <elist> ) | )
    private SyntaxTreeNode fncalltail() {
        if (tokenList.peek().getType() == TokenType.TRPAR) {
            tokenList.pop(); // )
            return null;
        }

        SyntaxTreeNode elistNode = elist();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",")", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(TokenType.TRPAR);
            if (unrecoverable) { return new SyntaxTreeNode(TreeNodeType.NUNDEF); }
        }

        tokenList.pop(); // )

        return elistNode;
    }
    

    // <prlist> ::= <printitem> <prlisttail>
    private SyntaxTreeNode prlist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NPRLST);
        node.setFirstChild(printitem());
        SyntaxTreeNode tail = prlisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    
    // <prlisttail> ::= , <prlist> | ε
    private SyntaxTreeNode prlisttail() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            return null;
        }

        tokenList.pop(); // ,
        
        // TODO: This might be increadibly incorrect!
        return prlist();

    }
    
    // <printitem> ::= <expr> | <string>
    private SyntaxTreeNode printitem() {
        if (tokenList.peek().getType() == TokenType.TSTRG) {
            Token token = tokenList.pop();
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(token.getLexeme(), token);
            // TODO: Setting declaration to string might be needed!;
            return new SyntaxTreeNode(TreeNodeType.NSTRG, token, record);
        }
        return expr();
    }

    private boolean popTillTokenType(TokenType type) {
        if (tokenList.isEmpty()) {
            // break out of recursion
            return true;
        }
        while (tokenList.peek().getType() != type) {
            tokenList.pop();
            if (tokenList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean popTillTokenType(LinkedList<TokenType> types) {
        if (tokenList.isEmpty()) {
            // break out of recursion
            return true;
        }
        while (!types.contains(tokenList.peek().getType())) {
            tokenList.pop();
            if (tokenList.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
