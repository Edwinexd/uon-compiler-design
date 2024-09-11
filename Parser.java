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
        var SyntaxTree = programParse();
    }

    // <program> ::= CD24 <id> <globals> <funcs> <mainbody>
    private SyntaxTreeNode programParse()
    {
        Token token = tokenList.pop();
        Token lookAhead = tokenList.peek();
        if (token.getType() != TokenType.TCD24)
        {
            
            // Critical error
        }

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

        //<TreeBro>

    }


    // <globals> ::= <consts> <types> <arrays>
    private SyntaxTreeNode globals() {
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

    // <consts> ::= constants <initlist> | ε
    private SyntaxTreeNode consts() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TCONS) {
            initlist();
            return;
        }
        // Critical error, this function should not have been called
    }

    // <initlist> ::= <init> <initlisttail>
    private SyntaxTreeNode initlist() {
        init();
        initlisttail();
    }

    // <initlisttail> ::= , <init> <initslisttail> | ε
    private SyntaxTreeNode initlisttail() {
        if (tokenList.peek().getType() == TokenType.TCOMA) {
            tokenList.pop();
            init();
            initlisttail();
        }
    }

    // <init> ::= <id> = <expr>
    private SyntaxTreeNode init() {
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

    // <types> ::= typedef <typelist> | ε
    private SyntaxTreeNode types() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TTYPD) {
            typelist();
            return;
        }
        // Critical error, this function should not have been called
    }

    // <typelist> ::= <type> <typelisttail>
    private SyntaxTreeNode typelist() {
        type();
        typelisttail();
    }

    // <typelisttail> ::= <type> <typelisttail> | ε
    private SyntaxTreeNode typelisttail() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            type();
            typelisttail();
        }
        // this is an epsilon production
    }

    // <type> ::= <structid> def <fields> end
    private SyntaxTreeNode type() {
        // TODO: If structid or if typeid, call the correct function
        typestruct();
        typetype();
    }



    // <type> ::= <typeid> def array [ <expr> ] of <structid> end

// ?????????????????????????????????????????????

    private SyntaxTreeNode typestruct() {
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
        sdecl();
        fieldstail();
    }

    // <fieldstail> ::= <sdecl> <fieldstail> | ε
    private SyntaxTreeNode fieldstail() {
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            sdecl();
            fieldstail();
        }
        // this is an epsilon production
    }

    private SyntaxTreeNode sdecl() {
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

    private SyntaxTreeNode stypeOrStructid() {
        if (tokenList.peek().getType() == TokenType.TINTG || tokenList.peek().getType() == TokenType.TFLOT || tokenList.peek().getType() == TokenType.TBOOL) {
            stype();
            return;
        }
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            Token idToken = tokenList.pop();
            return;
        }

    }

    private SyntaxTreeNode stype() {
        Token token = tokenList.pop();
        if (token.getType() == TokenType.TINTG || token.getType() == TokenType.TFLOT || token.getType() == TokenType.TBOOL) {
            return;
        }
        // Critical error, this function should not have been called due to lookahead
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
    private void funcs() {
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
    private void mainbody() {

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


    // <sdecl> ::= <id> : <stypeOrStructid>
    private SyntaxTreeNode sdecl() {
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

    // <stypeOrStructid> ::= <stype> | <structid>
    private SyntaxTreeNode stypeOrStructid() {
        if (tokenList.peek().getType() == TokenType.TINTG || tokenList.peek().getType() == TokenType.TFLOT || tokenList.peek().getType() == TokenType.TBOOL) {
            stype();
            return;
        }
        if (tokenList.peek().getType() == TokenType.TIDEN) {
            Token idToken = tokenList.pop();
            return;
        }

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

}
