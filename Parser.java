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
// <stat> ::= <repstat> | <asgnstat> | <iostat> | <callstat> | <returnstat>
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

// <callstat> ::= <id> ( <elist> ) | <id> ( )

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

// <expr> ::= <term> | <expr> <exprtail>
// <exprtail> ::= + <term> | - <term>
// <term> ::= <fact> | <term> <termtail>
// <termtail> ::= * <fact> | / <fact> | % <fact>
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

    public void InitiateParsing()
    {
        programParse();
    }

    private void programParse()
    {
        Token token = tokenList.pop();
        Token lookAhead = tokenList.peek();
        if (token.getType() == TokenType.TCD24)
        {
            // Global
            if (lookAhead.getType() == TokenType.TCONS)
            {
                globals();
            }
            // Functions
            else if (lookAhead.getType() == TokenType.TFUNC)
            {
                funcs();
            }
            // Main Body
            else if (lookAhead.getType() == TokenType.TMAIN)
            {
                mainBodyParse();
            }

        }
        else
        {
            // Critical error
        }

        //<TreeBro>

    }

    private void globals() {
        if (tokenList.peek().getType() == TokenType.TCONS) {
            consts();
        }
        if (tokenList.peek().getType() == TokenType.TTYPD) {
            types();
        }
        if (tokenList.peek().getType() == TokenType.TARRD) {
            arrays();
        }

    }

    private void consts() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TCONS) {
            initlist();
            return;
        }
        // Critical error, this function should not have been called
    }

    private void initlist() {
        init();
        initlisttail();
    }

    private void initlisttail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            init();
            initlisttail();
        }
    }

    private void init() {
        // pop the id token
        Token idToken = tokenList.pop();
        if (idToken.getType() != TokenType.TIDEN) {
            // Critical error
            return;
        }
        currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (tokenList.peek().getType() != TokenType.TEQUL) {
            // Critical error
            return;
        }
        // We don't really care about storing the TEQUL token
        tokenList.pop();
        // Next will be an expression
        var expressionNode = expr();
        // TODO Build tree node which will consist of idToken node and expressionNode

    }

    private void types() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TTYPD) {
            typelist();
            return;
        }
        // Critical error, this function should not have been called
    }

    private void typelist() {
        type();
        typelisttail();
    }

    private void typelisttail() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            type();
            typelisttail();
        }
        // this is an epsilon production
    }

    private void type() {
        // TODO: If structid or if typeid, call the correct function
        typestruct();
        typetype();
    }

    private void typestruct() {
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
        var fieldsNode = fields();
        if (tokenList.peek().getType() != TokenType.TEND) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about end keyword just has to be there
        // TODO Build tree node which will consist of idToken node and fieldsNode
    }

    private void typetype() {
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

    private void fields() {
        sdecl();
        fieldstail();
    }

    private void fieldstail() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            sdecl();
            fieldstail();
        }
        // this is an epsilon production
    }

    private void sdecl() {
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
        stypeOrStructid();
    }

    private void stypeOrStructid() {
        if (tokenList.peek().getType() == TokenType.TINTG || tokenList.peek().getType() == TokenType.TFLOT || tokenList.peek().getType() == TokenType.TBOOL) {
            stype();
            return;
        }
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            Token idToken = tokenList.pop();
            return;
        }

    }
    
    private void stype() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TINTG || token.getType() == TokenType.TFLOT || token.getType() == TokenType.TBOOL) {
            return;
        }
        // Critical error, this function should not have been called due to lookahead
    }

    private void arrays() {
        if (tokenList.peek().getType() == TokenType.TARRD) {
            arrdecls();
            return;
        }
        // Critical error, this function should not have been called
    }

    private void arrdecls() {
        arrdecl();
        arrdeclstail();
    }

    private void arrdeclstail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            arrdecl();
            arrdeclstail();
        }
        // this is an epsilon production
    }

    private void arrdecl() {
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

    private void funcs() {
        funcsPrime();
    }

    private void funcsPrime() {
        if (tokenList.peek().getType() == TokenType.TFUNC) {
            func();
            funcsPrime();
        }
        // this is an epsilon production
    }

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
        if (tokenList.peek().getType() != TokenType.TLPAR) {
            // Critical error
            return;
        }
        tokenList.pop(); // dont care about left parenthesis keyword just has to be there
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

    private void rtype() {
        if (tokenList.peek().getType() == TokenType.TVOID) {
            tokenList.pop();
            return;
        }
        stype();

    }

    private void plist() {
        if (tokenList.peek().getType() == TokenType.TIDEN || tokenList.peek().getType() == TokenType.TCONST) {
            params();
            return;
        }
        // this is an epsilon production
    }

    private void params() {
        param();
        paramsPrime();
    }

    private void paramsPrime() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            param();
            paramsPrime();
        }
        // this is an epsilon production
    }

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

    private void parammaybeconst() {
        if (tokenList.peek().getType() == TokenType.TCONS) {
            tokenList.pop();
            return;
        }
        // this is an epsilon production
    }

    private void paramtail() {
        // TODO: No idea how to differentiate between them all
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            typeid();
            return;
        }
        stypeOrStructid();
    }

    private void funcbody() {

    }

    private void locals() {

    }

    private void dlist() {

    }

    private void dlistPrime() {

    }

    private void decl() {

    }

    private void decltail() {

    }


}
