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

// <fncall> ::= <id> ( <elist> ) | <id> ( )

// <prlist> ::= <printitem> <prlisttail>
// <prlisttail> ::= , <prlist> | ε
// <printitem> ::= <expr> | <string>

// <id>, <structid>, <typeid> are all simply identifier tokens returned by the scanner.
// <intlit>, <reallit> and <string> are also special tokens returned by the scanner.

import java.util.LinkedList;

// We are intentionally breaking naming conventions here to match the CD24 grammar
public class Parser 
{
    private SymbolTable rootSymbolTable = new SymbolTable();
    private SymbolTable currentSymbolTable = rootSymbolTable;
    public LinkedList<Token> tokenList = new LinkedList<Token>();  

    public Parser(LinkedList<Token> list) 
    {
        tokenList = list;
    }

    public void initParsing()
    {
        SyntaxTreeNode syntaxTree = programParse();
    }

    // <program> ::= CD24 <id> <globals> <funcs> <mainbody>
    private SyntaxTreeNode programParse()
    {

        if (tokenList.peek().getType() != TokenType.TCD24)
        {
            // Critical error
        }
        tokenList.pop(); // CD24
        if (tokenList.peek().getType() != TokenType.TIDEN)
        {
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
        }
        // Functions
        if (tokenList.peek().getType() == TokenType.TFUNC)
        {
            SyntaxTreeNode funcs = funcs();
            rootNode.setFirstChild(funcs);
        }
        if (tokenList.peek().getType() != TokenType.TMAIN)
        {
            // Critical error, someone decided not to include a mandatory main function
            throw new RuntimeException("No main function found :(");
        }

        SyntaxTreeNode mainBody = mainbody();
        rootNode.setFirstChild(mainBody);

        return rootNode;
    }


    // <globals> ::= <consts> <types> <arrays>
    private SyntaxTreeNode globals() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NGLOB);
        if (tokenList.peek().getType() == TokenType.TCONS) {
            SyntaxTreeNode constsNode = consts();
            node.setFirstChild(constsNode);
        }
        if (tokenList.peek().getType() == TokenType.TTYPD) {
            SyntaxTreeNode typesNode = types();
            node.setSecondChild(typesNode);
        }
        if (tokenList.peek().getType() == TokenType.TARRD) {
            SyntaxTreeNode arraysNode = arrays();
            node.setThirdChild(arraysNode);
        }
        return node;
    }

    // <consts> ::= constants <initlist> | ε
    private SyntaxTreeNode consts() {
        // this does not produce its own node and just returns the initlist node
        if (tokenList.peek().getType() != TokenType.TCONS) {
            // Should not really have been called
            // Critical error, this function should not have been called
            throw new RuntimeException("Critical error, this function should not have been called");
        }
        tokenList.pop(); // constants
        return initlist();
    }

    // <initlist> ::= <init> <initlisttail>
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

    // <initlisttail> ::= , <init> <initslisttail> | ε
    private SyntaxTreeNode initlisttail() {
        if (tokenList.peek().getType() != TokenType.TCOMA) {
            // this is an epsilon production
            return null;
        }
        tokenList.pop(); // ,
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NILIST);
        node.setFirstChild(init());
        SyntaxTreeNode tail = initlisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }

    // <init> ::= <id> = <expr>
    private SyntaxTreeNode init() {
        // pop the id token
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Should not really have been called
            throw new RuntimeException("Critical error, this function should not have been called");
        }
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        // TODO Check if declarationtype here should be constant or int/float/bool etc.
        record.setDeclaration(Declaration.CONSTANT);

        if (tokenList.peek().getType() != TokenType.TEQUL) {
            throw new RuntimeException("Invalid syntax, expected =");
        }
        tokenList.pop(); // =

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NINIT, idToken, record);
        node.setFirstChild(expr());
        return node;
    }

    // <types> ::= typedef <typelist> | ε
    private SyntaxTreeNode types() {
        // this does not produce its own node and just returns the typelist node
        if (tokenList.peek().getType() != TokenType.TTYPD) {
            // Critical error, this function should not have been called
            throw new RuntimeException("Critical error, this function should not have been called");
        }
        tokenList.pop(); // typedef
        return typelist();
    }

    // <typelist> ::= <type> <typelisttail>
    private SyntaxTreeNode typelist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NTYPEL);
        node.setFirstChild(type());
        SyntaxTreeNode tail = typelisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }

    // <typelisttail> ::= <type> <typelisttail> | ε
    private SyntaxTreeNode typelisttail() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // this is an epsilon production
            return null;
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NTYPEL);
        node.setFirstChild(type());
        SyntaxTreeNode tail = typelisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }

    // <type> ::= N/A (choosing which type to parse based on lookahead)
    /*
     * More or less a "proxy" for calling the correct type function.
     */
    private SyntaxTreeNode type() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            throw new RuntimeException("Critical error, expected an identifier");
        }
        Token peekToken = tokenList.peek();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(peekToken.getLexeme(), peekToken);
        if (record.getDeclaration().isPresent() && record.getDeclaration().get() == DeclarationType.STRUCT_TYPE) {
            return typestruct();
        } else if (record.getDeclaration().isPresent() && record.getDeclaration().get() == DeclarationType.ARRAY_TYPE) {
            return typetype();
        }
        throw new RuntimeException("Critical error, expected a struct or array type");
    }

    // <type> ::= <structid> def <fields> end
    private SyntaxTreeNode typestruct() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NRTYPE);
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            throw new RuntimeException("Critical error, expected an identifier");
        }
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.STRUCT_TYPE);
        node.setNodeValue(idToken);
        node.setValueRecord(record);
        if (tokenList.peek().getType() != TokenType.TTDEF) {
            throw new RuntimeException("Critical error, expected def keyword");
        }
        tokenList.pop(); // def
        node.setFirstChild(fields());
        if (tokenList.peek().getType() != TokenType.TTEND) {
            throw new RuntimeException("Critical error, expected end keyword");
        }
        tokenList.pop(); // end
        return node;
    }

    // <type> ::= <typeid> def array [ <expr> ] of <structid> end
    private SyntaxTreeNode typetype() {
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        if (tokenList.peek().getType() != TokenType.TTDEF) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about def keyword just has to be there
        if (tokenList.peek().getType() != TokenType.TARAY) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about array keyword just has to be there
        if (tokenList.peek().getType() != TokenType.TLBRK) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about left bracket keyword just has to be there
        var exprNode = expr();
        if (tokenList.peek().getType() != TokenType.TRBRK) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about right bracket keyword just has to be there
        if (tokenList.peek().getType() != TokenType.TTTOF) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about of keyword just has to be there
        var structidNode = structid();
        if (tokenList.peek().getType() != TokenType.TEND) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about end keyword just has to be there
        // TODO Build tree node which will consist of idToken node, exprNode, and structidNode
    }

    // <fields> ::= <sdecl> <fieldstail>
    private SyntaxTreeNode fields() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFLIST);
        node.setFirstChild(sdecl());
        SyntaxTreeNode tail = fieldstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }

    // <fieldstail> ::= <sdecl> <fieldstail> | ε
    private SyntaxTreeNode fieldstail() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            return null;
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFLIST);
        node.setFirstChild(sdecl());
        SyntaxTreeNode tail = fieldstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }

    // <sdecl> ::= <id> : <stypeOrStructid>
    private SyntaxTreeNode sdecl() {
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            throw new RuntimeException("Critical error, expected an identifier");
        }
        Token idToken = tokenList.pop();
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            throw new RuntimeException("Critical error, expected colon");
        }
        tokenList.pop(); // :
        TreeNodeType outType;
        Declaration typeDeclaration = stypeOrStructid();
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

    // <arrays> ::= arraydef <arrdecls> | ε
    private SyntaxTreeNode arrays() {
        if (tokenList.peek().getType() == TokenType.TARRD) {
            arrdecls();
            return;
        }
        // Critical error, this function should not have been called
    }



    // <arrdecls> ::= <arrdecl> <arrdeclstail>
    private SyntaxTreeNode arrdecls() {
        arrdecl();
        arrdeclstail();
    }

    // <arrdeclstail> ::= , <arrdecl> <arrdeclstail> | ε 
    private SyntaxTreeNode arrdeclstail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            arrdecl();
            arrdeclstail();
        }
        // this is an epsilon production
    }

    // <arrdecl> ::= <id> : <typeid>
    private SyntaxTreeNode arrdecl() {
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about colon keyword just has to be there
        if (tokenList.peek().getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        Token idToken2 = tokenList.pop();
        // TODO Build tree node which will consist of idToken node and idToken2 node

    }

    // <funcs> ::= <funcPrime> 
    private SyntaxTreeNode funcs() {
        funcsPrime();
    }

    // <funcPrime> ::= <func> <funcPrime> | ε
    private void funcsPrime() {
        if (tokenList.peek().getType() == TokenType.TFUNC) {
            func();
            funcsPrime();
        }
        // this is an epsilon production
    }

    // <func> ::= func <id> ( <plist> ) : <rtype> <funcbody>
    private void func() {
        if (tokenList.peek().getType() != TokenType.TFUNC) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about func keyword just has to be there
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(DeclarationType.FUNCTION);
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about left parenthesis keyword just has to be there
        // TODO: Should be part of func parameters + scope
        plist();
        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about right parenthesis keyword just has to be there
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about colon keyword just has to be there
        rtype();
        // Enter the function's scope
        currentSymbolTable = record.getScope();
        funcbody();
        // Exit the function's scope
        currentSymbolTable = currentSymbolTable.getParent();

        // TODO: Build tree node of bunch of stuff
    }

    // <rtype> ::= <stype> | void
    private void rtype() {
        if (tokenList.peek().getType() == TokenType.TVOID) {
            tokenList.pop();
            return;
        }
        stype();

    }

    // <plist> ::= <params> | ε
    private void plist() {
        if (tokenList.peek().getType() == TokenType.TIDEN || tokenList.peek().getType() == TokenType.TCONST) {
            params();
            return;
        }
        // this is an epsilon production
    }

    
    // <params> ::= <param> <paramsPrime>
    private void params() {
        param();
        paramsPrime();
    }

    // <paramsPrime> ::= , <param> <paramsPrime> | ε
    private void paramsPrime() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            param();
            paramsPrime();
        }
        // this is an epsilon production
    }

    // <param> ::= <parammaybeconst> <id> : <paramtail>
    private void param() {
        // TODO: Remember that param is const (if it is)
        parammaybeconst();
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about colon keyword just has to be there
        paramtail();
    }

    // <parammaybeconst> ::= const | ε
    private void parammaybeconst() {
        if (tokenList.peek().getType() == TokenType.TCONS) {
            tokenList.pop();
            return;
        }
        // this is an epsilon production
    }

    // <paramtail> ::= <typeid> | <stypeOrStructid>
    private void paramtail() {
        // TODO: No idea how to differentiate between them all
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            typeid();
            return;
        }
        stypeOrStructid();
    }

    // <funcbody> ::= <locals> begin <stats> end
    private void funcbody() {
        locals();
        if (tokenList.peek().getType() != TokenType.TBEGN) {
            // Critical error
            return;
        }
        tokenList.pop();

        stats();
        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            return;
        }
        tokenList.pop();

    }

    // <locals> ::= <dlist> | ε
    private void locals() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            dlist();
        }

        // fancy e thing

    }

    
    // <dlist> ::= <decl> <dlistPrime>
    private void dlist() {
        decl();
        dlistPrime();

    }

    
    // <dlistPrime> ::= , <dlist> | ε
    private void dlistPrime() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            dlist();
        }

        // fancy e thang
    }

    
    // <decl> ::=  <id> : <decltail>
    private void decl() {
        Token idToken = tokenList.pop();

        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        if (tokenList.peek().getType() != TokenType.TCOLN) {
            // Critical error
            return;
        }

        tokenList.pop(); // :

        decltail();

    }

    // <decltail> ::= <typeid> | <stypeOrStructid>
    private void decltail() {

        if (tokenList.peek().getType() == TokenType.TIDEN) {
            Token idToken = tokenList.pop();
            return;
        }

        stypeOrStructid();

    }


    // <mainbody> ::= main <slist> begin <stats> end CD24 <id>
    private SyntaxTreeNode mainbody() {

        if (tokenList.peek().getType() != TokenType.TMAIN) {
            // Critical error
            return;
        }

        tokenList.pop(); // main

        slist();

        if (tokenList.peek().getType() != TokenType.TBEGN) {
            // Critical error
            return;
        }

        tokenList.pop(); // begin

        stats();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            return;
        }

        tokenList.pop(); // end

        if (tokenList.peek().getType() != TokenType.TCD24) {
            // Critical error
            return;
        }

        tokenList.pop(); // CD24

        Token idToken = tokenList.pop();

        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }

    }
    
    // <slist> ::= <sdecl> <slistPrime>
    private void slist() {
        sdecl();
        slistPrime();
    }

    // <slistPrime> ::= , <sdecl> <slistPrime> | ε
    private void slistPrime() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            sdecl();
            slistPrime();
        }

        //// eeeeeee bruv

    }

    // <stypeOrStructid> ::= <stype> | <structid>
    private Declaration stypeOrStructid() {
        Token peekedToken = tokenList.peek();
        TokenType peekedType = peekedToken.getType();
        if (peekedType == TokenType.TINTG) {
            return Declaration.INT;
        } else if (peekedType == TokenType.TFLOT) {
            return Declaration.FLOAT;
        } else if (peekedType == TokenType.TBOOL) {
            return Declaration.BOOL;
        }
        if (peekedType != TokenType.TIDEN) {
            throw new RuntimeException("Critical error, expected a struct identifier or a type");
        }
        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (record.getDeclaration().isPresent() && !record.getDeclaration().get().equals(Declaration.STRUCT_TYPE)) {
            // TODO Incorrect type
            throw new RuntimeException("Critical error, expected a struct identifier");
        }
        return Declaration.structOfType(record);
    }

    // <stype> ::= int | float | bool
    private SyntaxTreeNode stype() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TINTG || token.getType() == TokenType.TFLOT || token.getType() == TokenType.TBOOL) {
            return;
        }
        // Critical error, this function should not have been called due to lookahead
    }



    // <stats> ::= <stat>; <statstail> | <strstat> <statstail>
    private void stats() {
        if (tokenList.peek().getType() == TokenType.TTFOR || 
            tokenList.peek().getType() == TokenType.TIFTH ||
            tokenList.peek().getType() == TokenType.TSWTH ||
            tokenList.peek().getType() == TokenType.TTTDO) {

            strstat();

            statstail();

        } else {

            stat();

            if (tokenList.peek().getType() != TokenType.TSEMI) {
                // Critical error
                return;
            }

            tokenList.pop(); // ;

            statstail();
        }
    }


    // <statstail> ::= <stat>; <statstail> | <strstat> <statstail> | ε
    private void statstail() {
        if (tokenList.peek().getType() == TokenType.TTFOR || 
            tokenList.peek().getType() == TokenType.TIFTH ||
            tokenList.peek().getType() == TokenType.TSWTH ||
            tokenList.peek().getType() == TokenType.TTTDO) {

            strstat();

            statstail();

        } else if (tokenList.peek().getType() != TokenType.TSEMI) {

            stat();

            if (tokenList.peek().getType() != TokenType.TSEMI) {
                // Critical error
                return;
            }

            tokenList.pop(); // ;

            statstail();
        }
        // eeee eee eee ee e
    }


    // <strstat> ::= <forstat> | <ifstat> | <switchstat> | <dostat>
    private void strstat() {
        if (tokenList.peek().getType() == TokenType.TTFOR) {

            forstat();

        } else if (tokenList.peek().getType() == TokenType.TIFTH) {

            ifstat();

        } else if (tokenList.peek().getType() == TokenType.TSWTH) {

            switchstat();

        } else if (tokenList.peek().getType() == TokenType.TTTDO) {

            dostat();

        }
    }


    // <stat> ::= <repstat> | <iostat> | <returnstat> | <asgnstatorcallstat>
    private void stat() {

        if (tokenList.peek().getType() == TokenType.TREPT) {

            repstat();

        } else if (tokenList.peek().getType() == TokenType.TINPT || 
                    tokenList.peek().getType() == TokenType.TPRNT || 
                    tokenList.peek().getType() == TokenType.TPRLN) {

            iostat();

        } else if (tokenList.peek().getType() == TokenType.TRETN) {

            returnstat();

        } else if (tokenList.peek().getType() == TokenType.TIDEN) {

            asgnstatorcallstat();

        } else {
            // Error
        }
        
    }
    // <asgnstatorcallstat> ::= <id> <asgnstatorcallstattail>
    private void asgnstatorcallstat() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {

            tokenList.pop(); // <id>

            asgnstatorcallstattail();

        } else {
            // Error
        }
    }
    // <asgnstatorcallstattail> ::= <vartail> | ( <callstattail>
    private void asgnstatorcallstattail() {
        TokenType currentType = tokenList.peek().getType();

        if (currentType == TokenType.TLBRK) {

            vartail();

        } else if (currentType == TokenType.TLPAR) {

            tokenList.pop(); // (

            callstattail();

        } else {
            // Error
        }
    }

    // <forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
    private void forstat() {

        tokenList.pop(); // for

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        asgnlist();

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // ;

        boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )

        stats();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            return;
        }

        tokenList.pop(); // end
    }
    // <repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>
    private void repstat() {

        tokenList.pop(); // repeat

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        asgnlist();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )

        stats();

        if (tokenList.peek().getType() != TokenType.TUNTL) {
            // Critical error
            return;
        }

        tokenList.pop(); // until

        boolParse();
    }
    // <dostat> ::= do <stats> while ( <bool> ) end
    private void dostat() {

        tokenList.pop(); // do

        stats();

        if (tokenList.peek().getType() != TokenType.TWHIL) {
            // Critical error
            return;
        }

        tokenList.pop(); // while

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            return;
        }

        tokenList.pop(); // end
    }
    // <asgnlist> ::= <alist> | ε
    private void asgnlist() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            alist();
        }
        // EEEEEEEEEEEEEEEEEEEEEEEEEEE
    }
    // <alist> ::=<asgnstat> <alisttail>
    private void alist() {
        asgnstat();
        alisttail();
    }
    // <alisttail> ::= , <asgnstat> <alisttail> | ε
    private void alisttail() {

        if (tokenList.peek().getType() == TokenType.TCOMA) {

            tokenList.pop(); // ,

            asgnstat();

            alisttail();

        }
    }

    // <ifstat> ::= if ( <bool> ) <stats> <ifstattail> end
    private void ifstat() {

        tokenList.pop(); // if

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )

        stats();

        ifstattail();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            return;
        }

        tokenList.pop(); // end

    }
    // <ifstattail> ::= else <stats> | elif (<bool>) <stats> | ε
    private void ifstattail() {
        tokenList.pop();

        if (tokenList.peek().getType() == TokenType.TELSE) {
            
            tokenList.pop(); // else

            stats();

        } else if (tokenList.peek().getType() == TokenType.TELIF) {
            
            if (tokenList.peek().getType() != TokenType.TLPAR) {
                // Critical error
                return;
            }

            tokenList.pop(); // (

            boolParse();

            if (tokenList.peek().getType() != TokenType.TRPAR) {
                // Critical error
                return;
            }

            tokenList.pop(); // )

            stats();

        } else {
            // eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
        }
    }

    // <switchstat> ::= switch ( <expr> ) begin <caselist> end
    private void switchstat() {
        if (tokenList.peek().getType() != TokenType.TSWTH) {
            //er Raw
        }
        tokenList.pop(); // switch
            
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        expr();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )

        if (tokenList.peek().getType() != TokenType.TBEGN) {
            // Critical error
            return;
        }

        tokenList.pop(); // begin

        caselist();

        if (tokenList.peek().getType() != TokenType.TTEND) {
            // Critical error
            return;
        }

        tokenList.pop(); // end
    }
    // <caselist> ::= case <expr> : <stats> break ; <caselist> | default : <stats>
    private void caselist() {
        if (tokenList.peek().getType() == TokenType.TCASE) {

            tokenList.pop(); // case

            expr();

            if (tokenList.peek().getType() != TokenType.TCOLN) {
                // Critical error
                return;
            }
            
            tokenList.pop(); // :

            stats();

            if (tokenList.peek().getType() != TokenType.TBREK) {
                // Critical error
                return;
            }

            tokenList.pop(); // break

            if (tokenList.peek().getType() != TokenType.TSEMI) {
                // Critical error
                return;
            }

            tokenList.pop(); // ;

            caselist();

        } else if (tokenList.peek().getType() == TokenType.TDFLT) {

            tokenList.pop(); // default

            if (tokenList.peek().getType() != TokenType.TCOLN) {
                // Critical error
                return;
            }

            tokenList.pop(); // :

            stats();

        } else {
            // error
        }
    }
    // <asgnstat> ::= <var> <asgnop> <bool>
    private void asgnstat() {
        var();
        asgnop();
        boolParse();
    }
    // <asgnop> :: == | += | -= | *= | /=
    private void asgnop() {
        if (tokenList.peek().getType() == TokenType.TEQEQ) {
            tokenList.pop();
            // in tree
            return;
        } else if (tokenList.peek().getType() == TokenType.TPLEQ) {
            tokenList.pop();
            // in tree
            return;
        } else if (tokenList.peek().getType() == TokenType.TMNEQ) {
            tokenList.pop();
            // in tree
            return;
        }else if (tokenList.peek().getType() == TokenType.TSTEQ) {
            tokenList.pop();
            // in tree
            return;
        } else if (tokenList.peek().getType() == TokenType.TDVEQ) {
            tokenList.pop();
            // in tree
            return;
        }
        // Critical error
    }

    // <iostat> ::= input <vlist> | print <prlist> | printline <prlist>
    private void iostat() {
        if (tokenList.peek().getType() == TokenType.TINPT) {

            tokenList.pop(); // input

            vlist();

        } else if (tokenList.peek().getType() == TokenType.TPRNT) {

            tokenList.pop(); // print

            prlist();

        } else if (tokenList.peek().getType() == TokenType.TPRLN) {

            tokenList.pop(); // printline

            prlist();

        } else {
            // Critical error
        }
    }

    // <callstat> ::= <id> ( <callstattail>
    private void callstat() {

        Token idToken = tokenList.pop();

        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        callstattail();
    }

    // <callstattail> ::= <elist> ) | )
    private void callstattail() {

        if (tokenList.peek().getType() == TokenType.TRPAR) {

            tokenList.pop(); // )

        } else {

            elist();

            if (tokenList.peek().getType() != TokenType.TRPAR) {
                // Critical error
                return;
            }

            tokenList.pop(); // )

        }
    }

    // <returnstat> ::= return void | return <expr>
    private void returnstat() {
        if (tokenList.peek().getType() == TokenType.TRETN) {

            tokenList.pop(); // return

            if (tokenList.peek().getType() == TokenType.TVOID) {

                tokenList.pop(); // void

            } else {

                expr();

            }

        } else {
            // Critical error
        }
    }
    // <vlist> ::= <var> <vlisttail>
    private void vlist() {
        var();
        vlisttail();
    }
    // <vlisttail> ::= , <vlisttail> | ε
    private void vlisttail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {

            tokenList.pop(); // ,

            vlisttail();

        } else {
            // Eee
        }
    }
    // <var> ::= <id><vartail>
    private void var() {
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        vartail();
    }
    // <vartail> ::= [<expr>]<vartailtail> | ε
    private void vartail() {
        if (tokenList.peek().getType() == TokenType.TLBRK) {

            tokenList.pop(); // [

            expr();

            if (tokenList.peek().getType() != TokenType.TRBRK) {
                // Critical error
                return;
            }

            tokenList.pop(); // ]

            vartailtail();

        } else {
            // Eee
        }
    }
    // <vartailtail> ::= . <id> | ε
    private void vartailtail() {
        if (tokenList.peek().getType() == TokenType.TDOTT) {

            tokenList.pop(); // .

            Token idToken = tokenList.pop();

            if (idToken.getType() != TokenType.TIDEN) {
                // Critical error
                return;
            }

        } else {
            // Eee
        }
    }
    // <elist> ::= <bool> <elisttail>
    private void elist() {
        boolParse();
        elisttail();
    }
    // <elisttail> ::= , <elist> | ε
    private void elisttail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {

            tokenList.pop(); // ,

            elist();

        } else {
            // Eee
        }
    }
    // <bool> ::= not <bool> | <bool><logop> <rel> | <rel>
    private void boolParse() {
        if (tokenList.peek().getType() == TokenType.TNOTT) {

            tokenList.pop(); // not

            boolParse();

        } else {

            rel();

            if (tokenList.peek().getType() == TokenType.TTAND || 
                tokenList.peek().getType() == TokenType.TTTOR || 
                tokenList.peek().getType() == TokenType.TTXOR) {

                tokenList.pop(); // logop

                rel();

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

            expr();

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
    }

    // TODO: Expr & exprtail may be incorrect!
    // <expr> ::= <term> <exprtail>
    private void expr() {
        term();
        exprtail();
    }
    // <exprtail> ::= + <expr> | - <expr> | ε
    private void exprtail() {
        if (tokenList.peek().getType() == TokenType.TPLUS || 
            tokenList.peek().getType() == TokenType.TMINS) {

            tokenList.pop(); // + or -

            expr();

        } else {
            // Eee
        }
    }
    // TODO: Term & termtail may be incorrect!
    // <term> ::= <fact> <termtail>
    private void term() {
        fact();
        termtail();
    }

    // <termtail> ::= * <term> | / <term> | ε
    private void termtail() {
        if (tokenList.peek().getType() == TokenType.TSTAR || 
            tokenList.peek().getType() == TokenType.TDIVD) {

            tokenList.pop(); // * or /

            term();

        } else {
            // Eee
        }
    }

    // <fact> ::= <exponent> <factPrime>
    private void fact() {
        exponent();
        factPrime();
    }

    // <factPrime> ::= ^ <exponent> <factPrime> | ε
    private void factPrime() {
        if (tokenList.peek().getType() == TokenType.TCART) {

            tokenList.pop(); // ^

            exponent();

            factPrime();

        } else {
            // Eee
        }
    }

     // <exponent> ::= <exponentNotBool> | <exponentBool>
     private void exponent() {
        if (tokenList.peek().getType() == TokenType.TLPAR) {

            exponentBool();

        } else {

            exponentNotBool();
            
        }
    }

    // <exponentNotBool> ::= <var> | <intlit> | <reallit> | <fncall> | true | false
    private void exponentNotBool() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {

            var();

        } else if (tokenList.peek().getType() == TokenType.TILIT) {

            tokenList.pop(); // intlit (int)

        } else if (tokenList.peek().getType() == TokenType.TFLIT) {

            tokenList.pop(); // reallit (float)

        } else if (tokenList.peek().getType() == TokenType.TTRUE || 
                   tokenList.peek().getType() == TokenType.TFALS) {

            tokenList.pop(); // true or false

        } else {

            fncall();

        }
    }

    // <exponentBool> ::= ( <bool> )
    public void exponentBool() {
            
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        boolParse();

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )

    }


    // <fncall> ::= <id> ( <elist> ) | <id> ( )
    private void fncall() {
        Token idToken = tokenList.pop();

        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }

        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // (

        if (tokenList.peek().getType() != TokenType.TRPAR) {

            elist();

        }

        if (tokenList.peek().getType() != TokenType.TRPAR) {
            // Critical error
            return;
        }

        tokenList.pop(); // )
    }

    // <prlist> ::= <printitem> <prlisttail>
    private void prlist() {
        printitem();
        prlisttail();
    }
    
    // <prlisttail> ::= , <prlist> | ε
    private void prlisttail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {

            tokenList.pop(); // ,

            prlist();

        } else {
            // Eee
        }
    }
    
    // <printitem> ::= <expr> | <string>
    private void printitem() {
        if (tokenList.peek().getType() == TokenType.TSTRG) {

            // i think tree?

            tokenList.pop(); // string

        } else {

            expr();

        }
    }
}
