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
import java.util.List;

/**
 * Parser for CD24 language
 * Note: We are intentionally breaking naming conventions here (in some places)
 * to match the grammar
 * 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class Parser {
    private SymbolTable rootSymbolTable = new SymbolTable();
    private SymbolTable currentSymbolTable = rootSymbolTable;
    public LinkedList<Token> tokenList = new LinkedList<Token>();
    private TokenOutput tokenOutput;
    private boolean unrecoverable = false;

    public Parser(LinkedList<Token> list, TokenOutput tokenOutput) {
        tokenList = list;
        this.tokenOutput = tokenOutput;
    }

    public SyntaxTreeNode parse() {
        return programParse();
    }

    public List<SyntaxTreeNode> traverse(SyntaxTreeNode root) {
        if (root == null) {
            return new LinkedList<>();
        }
        LinkedList<SyntaxTreeNode> list = new LinkedList<>();
        traverseNode(root, list);
        return list;

    }

    private void traverseNode(SyntaxTreeNode node, List<SyntaxTreeNode> list) {
        if (node == null) {
            return;
        }

        list.add(node);
        traverseNode(node.getFirstChild().orElse(null), list);
        traverseNode(node.getSecondChild().orElse(null), list);
        traverseNode(node.getThirdChild().orElse(null), list);
    }

    private boolean typeAtPeek(TokenType... types) {
        return !tokenList.isEmpty() && Arrays.stream(types).anyMatch(t -> t == tokenList.peek().getType());
    }

    private boolean notTypeAtPeek(TokenType... types) {
        return !tokenList.isEmpty() && Arrays.stream(types).noneMatch(t -> t == tokenList.peek().getType());
    }

    private void safePeek(String tokenTypeExplanation, TokenType... types) {
        // TOOD: If find keyword or delimiter, stop le pop!
        if (!tokenList.isEmpty() && typeAtPeek(types)) {
            return;
        }

        if (!tokenList.isEmpty()) {
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",
                    tokenTypeExplanation, tokenList.peek().getLine(), tokenList.peek().getColumn()));
            unrecoverable = popTillTokenType(types);
        } else {
            unrecoverable = true;
        }
    }

    private SyntaxTreeNode getErrorNode() {
        return new SyntaxTreeNode(TreeNodeType.NUNDEF);
    }

    // #region <program> ::= CD24 <id> <globals> <funcs> <mainbody>
    private SyntaxTreeNode programParse() {
        safePeek("CD24", TokenType.TCD24);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // CD24

        safePeek("Identifier for CD24", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.PROGRAM);
        SyntaxTreeNode rootNode = new SyntaxTreeNode(TreeNodeType.NPROG, idToken, record);

        // Global
        if (typeAtPeek(TokenType.TCONS)) {
            SyntaxTreeNode globals = globals();
            rootNode.setFirstChild(globals);
        }

        // Functions
        if (typeAtPeek(TokenType.TFUNC)) {
            SyntaxTreeNode funcs = funcs();
            rootNode.setSecondChild(funcs);
        }

        // Someone decided not to include a mandatory main function
        safePeek("main", TokenType.TMAIN);
        if (unrecoverable) {
            return getErrorNode();
        }

        currentSymbolTable = record.getScope();
        SyntaxTreeNode mainBody = mainbody();
        currentSymbolTable = currentSymbolTable.getParent();
        rootNode.setThirdChild(mainBody);

        return rootNode;
    }
    // endregion

    // #region <globals> ::= <consts> <types> <arrays>
    private SyntaxTreeNode globals() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NGLOB);
        if (typeAtPeek(TokenType.TCONS)) {
            SyntaxTreeNode constsNode = consts();
            node.setFirstChild(constsNode);
        }
        if (typeAtPeek(TokenType.TTYPD)) {
            SyntaxTreeNode typesNode = types();
            node.setSecondChild(typesNode);
        }
        if (typeAtPeek(TokenType.TARRD)) {
            SyntaxTreeNode arraysNode = arrays();
            node.setThirdChild(arraysNode);
        }
        return node;
    }
    // endregion

    // #region <consts> ::= constants <initlist> | ε
    private SyntaxTreeNode consts() {
        // this does not produce its own node and just returns the initlist node
        safePeek("constants", TokenType.TCONS);
        if (unrecoverable) {
            return getErrorNode();
        }
        tokenList.pop(); // constants
        return initlist();
    }
    // endregion

    // #region <initlist> ::= <init> <initlisttail>
    private SyntaxTreeNode initlist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NILIST);
        node.setFirstChild(init());
        // for some reason they dont want us to use the second child when we have two
        // possible children
        SyntaxTreeNode tail = initlisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // endregion

    // #region <initlisttail> ::= , <init> <initslisttail> | ε
    private SyntaxTreeNode initlisttail() {
        if (notTypeAtPeek(TokenType.TCOMA)) {
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
    // endregion

    // #region <init> ::= <id> = <expr>
    private SyntaxTreeNode init() {
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        // TODO Check if declarationtype here should be constant or int/float/bool etc.
        record.setDeclaration(Declaration.CONSTANT);

        safePeek("=", TokenType.TEQUL);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // =

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NINIT, idToken, record);

        node.setFirstChild(expr());
        return node;
    }
    // endregion

    // #region <types> ::= typedef <typelist> | ε
    private SyntaxTreeNode types() {
        // this does not produce its own node and just returns the typelist node
        safePeek("typedef", TokenType.TTYPD);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // typedef

        return typelist();
    }
    // endregion

    // #region <typelist> ::= <type> <typelisttail>
    private SyntaxTreeNode typelist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NTYPEL);
        node.setFirstChild(type());
        SyntaxTreeNode tail = typelisttail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // #endregion

    // #region <typelisttail> ::= <type> <typelisttail> | ε
    private SyntaxTreeNode typelisttail() {
        if (notTypeAtPeek(TokenType.TIDEN)) {
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
    // #endregion

    // #region <type> ::= N/A (choosing which type to parse based on lookahead)
    /*
     * More or less a "proxy" for calling the correct type function.
     */
    // <type> ::= <typeid> def array <typetype> | <structid> def <typestruct>
    private SyntaxTreeNode type() {
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        safePeek("def", TokenType.TTDEF);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // def

        if (typeAtPeek(TokenType.TARAY)) {
            tokenList.pop(); // array
            return typetype(idToken);
        } else {
            return typestruct(idToken);
        }
    }
    // #endregion

    // #region <type> ::= <fields> end
    private SyntaxTreeNode typestruct(Token idToken) {
        SyntaxTreeNode fieldsNode = fields();

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // end

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.STRUCT_TYPE);

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NRTYPE, idToken, record);

        node.setFirstChild(fieldsNode);

        return node;
    }
    // endregion

    // #region <type> ::= [ <expr> ] of <structid> end
    private SyntaxTreeNode typetype(Token idToken) {
        safePeek("[", TokenType.TLBRK);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // [

        SyntaxTreeNode exprNode = expr();

        safePeek("]", TokenType.TRBRK);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // ]

        safePeek("of", TokenType.TTTOF);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // of

        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token structIdToken = tokenList.pop();

        // create record for new declaration
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(
                Declaration.arrayOfType(currentSymbolTable.getOrCreateToken(structIdToken.getLexeme(), structIdToken)));

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // end

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NATYPE, idToken, record);
        node.setFirstChild(exprNode);
        return node;
    }
    // endregion

    // #region <fields> ::= <sdecl> <fieldstail>
    private SyntaxTreeNode fields() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFLIST);
        node.setFirstChild(sdecl());
        SyntaxTreeNode tail = fieldstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // endregion

    // #region <fieldstail> ::= <sdecl> <fieldstail> | ε
    private SyntaxTreeNode fieldstail() {
        if (notTypeAtPeek(TokenType.TIDEN)) {
            // this is an epsilon production
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
    // endregion

    // #region <sdecl> ::= <id> : <stypeOrStructid>
    private SyntaxTreeNode sdecl() {
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        safePeek(":", TokenType.TCOLN);
        if (unrecoverable) {
            return getErrorNode();
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
    // endregion

    // #region <arrays> ::= arraydef <arrdecls> | ε
    private SyntaxTreeNode arrays() {
        // Note: this will not have been called if the next token is not arraydef so
        // should prob be removed
        safePeek("arraydef", TokenType.TARRD);
        if (unrecoverable) {
            return getErrorNode();
        }
        tokenList.pop(); // arraydef
        return arrdecls();
    }
    // endregion

    // #region <arrdecls> ::= <arrdecl> <arrdeclstail>
    private SyntaxTreeNode arrdecls() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NALIST);
        node.setFirstChild(arrdecl());
        SyntaxTreeNode tail = arrdeclstail();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // endregion

    // #region <arrdeclstail> ::= , <arrdecl> <arrdeclstail> | ε
    private SyntaxTreeNode arrdeclstail() {
        if (notTypeAtPeek(TokenType.TCOMA)) {
            // this is an epsilon production
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
    // endregion

    // #region <arrdecl> ::= <id> : <typeid>
    private SyntaxTreeNode arrdecl() {
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        safePeek(":", TokenType.TCOLN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // :

        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
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
    // endregion

    // #region <funcs> ::= <func> <funcs> | ε
    private SyntaxTreeNode funcs() {
        if (notTypeAtPeek(TokenType.TFUNC)) {
            return null;
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NFUNCS);
        node.setFirstChild(func());
        SyntaxTreeNode tail = funcs();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // endregion

    // #region <func> ::= func <id> ( <plist> ) : <rtype> <funcbody>
    private SyntaxTreeNode func() {
        safePeek("func", TokenType.TFUNC);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // func
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.FUNCTION);

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (
        // TODO: Should be part of func parameters + scope
        SyntaxTreeNode plistNode = plist();
        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        safePeek(":", TokenType.TCOLN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // :
        record.setReturnType(rtype());
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
    // endregion

    // #region <rtype> ::= <stype> | void
    private Declaration rtype() {
        if (typeAtPeek(TokenType.TVOID)) {
            tokenList.pop();
            return Declaration.VOID;
        }
        return stype();
    }
    // endregion

    // #region <plist> ::= <params> | ε
    private SyntaxTreeNode plist() {
        if (notTypeAtPeek(TokenType.TIDEN, TokenType.TCONS)) {
            return null;
        }
        return params();
    }
    // endregion

    // #region <params> ::= <param> <paramsPrime>
    private SyntaxTreeNode params() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NPLIST);
        node.setFirstChild(param());

        SyntaxTreeNode tail = paramsPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }
    // endregion

    // #region <paramsPrime> ::= , <param> <paramsPrime> | ε
    private SyntaxTreeNode paramsPrime() {
        if (notTypeAtPeek(TokenType.TCOMA)) {
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
    // endregion

    // #region <param> ::= <parammaybeconst> <id> : <paramtail>
    private SyntaxTreeNode param() {
        boolean isConst = parammaybeconst();

        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        safePeek(":", TokenType.TCOLN);
        if (unrecoverable) {
            return getErrorNode();
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
    // endregion

    // #region <parammaybeconst> ::= const | ε
    private boolean parammaybeconst() {
        if (notTypeAtPeek(TokenType.TCONS)) {
            return false;
        }
        tokenList.pop(); // const
        return true;
    }
    // endregion

    // #region <paramtail> ::= <typeid> | <stypeOrStructid>
    private Declaration paramtail(boolean isConst) {
        // TODO: This is wrong since stypeOrStructid could be ok...
        safePeek("Identifier", TokenType.TIDEN);
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
    // endregion

    // #region <funcbody> ::= <locals> begin <stats> end
    private SyntaxTreeNode[] funcbody() {
        SyntaxTreeNode[] nodes = new SyntaxTreeNode[2];
        nodes[0] = locals();
        safePeek("begin", TokenType.TBEGN);
        if (unrecoverable) {
            return new SyntaxTreeNode[] { new SyntaxTreeNode(TreeNodeType.NUNDEF),
                    new SyntaxTreeNode(TreeNodeType.NUNDEF) };
        }
        tokenList.pop(); // begin
        nodes[1] = stats();

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return new SyntaxTreeNode[] { new SyntaxTreeNode(TreeNodeType.NUNDEF),
                    new SyntaxTreeNode(TreeNodeType.NUNDEF) };
        }
        tokenList.pop(); // end

        return nodes;
    }
    // endregion

    // #region <locals> ::= <dlist> | ε
    private SyntaxTreeNode locals() {
        if (notTypeAtPeek(TokenType.TIDEN)) {
            return null;
        }
        return dlist();
    }
    // endregion

    // #region <dlist> ::= <decl> <dlistPrime>
    private SyntaxTreeNode dlist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NDLIST);
        node.setFirstChild(decl());
        SyntaxTreeNode tail = dlistPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }
        return node;
    }
    // endregion

    // #region <dlistPrime> ::= , <dlist> | ε
    private SyntaxTreeNode dlistPrime() {
        if (notTypeAtPeek(TokenType.TCOMA)) {
            return null;
        }
        tokenList.pop(); // ,
        return dlist();
    }
    // endregion

    // #region <decl> ::= <id> : <decltail>
    private SyntaxTreeNode decl() {
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        safePeek(":", TokenType.TCOLN);
        if (unrecoverable) {
            return getErrorNode();
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
    // endregion

    // #region <decltail> ::= <typeid> | <stypeOrStructid>
    private Declaration decltail() {
        return paramtail(false); // Same as paramtail but not const
    }
    // endregion

    // #region <mainbody> ::= main <slist> begin <stats> end CD24 <id>
    private SyntaxTreeNode mainbody() {
        safePeek("main", TokenType.TMAIN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // main

        SyntaxTreeNode slistNode = slist();

        safePeek("begin", TokenType.TBEGN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // begin

        SyntaxTreeNode statsNode = stats();

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // end

        safePeek("CD24", TokenType.TCD24);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // CD24

        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }
        Token idToken = tokenList.pop();

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (record.getDeclaration().isEmpty() || !record.getDeclaration().get().equals(Declaration.PROGRAM)) {
            // TODO End of main id should be the same as the program id
        }
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NMAIN, idToken, record);
        node.setFirstChild(slistNode);
        node.setSecondChild(statsNode);
        return node;
    }
    // endregion

    // #region <slist> ::= <sdecl> <slistPrime>
    private SyntaxTreeNode slist() {
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NSDLIST);
        node.setFirstChild(sdecl());

        SyntaxTreeNode tail = slistPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }
    // endregion

    // #region <slistPrime> ::= , <sdecl> <slistPrime> | ε
    private SyntaxTreeNode slistPrime() {
        if (notTypeAtPeek(TokenType.TCOMA)) {
            return null;
        }

        tokenList.pop(); // ,

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NSDLIST);
        node.setFirstChild(sdecl());

        SyntaxTreeNode tail = slistPrime();
        if (tail != null) {
            node.setThirdChild(tail);
        }

        return node;
    }
    // endregion

    // <stypeOrStructid> ::= <stype> | <structid>
    private Declaration stypeOrStructid() {
        safePeek("Struct Identifier", TokenType.TIDEN, TokenType.TINTG, TokenType.TFLOT, TokenType.TBOOL);
        // TODO: This one is special
        if (unrecoverable) {
            throw new RuntimeException("Critical error, expected a struct identifier or a type");
        }

        Token token = tokenList.pop();
        TokenType tokenType = token.getType();
        if (tokenType == TokenType.TINTG) {
            return Declaration.INT;
        } else if (tokenType == TokenType.TFLOT) {
            return Declaration.FLOAT;
        } else if (tokenType == TokenType.TBOOL) {
            return Declaration.BOOL;
        }

        Token idToken = token;
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        if (record.getDeclaration().isPresent() && !record.getDeclaration().get().equals(Declaration.STRUCT_TYPE)) {
            // TODO Incorrect type
            // unrecoverable = popTillTokenType(new
            // LinkedList<TokenType>(Arrays.asList(TokenType.TINTG, TokenType.TFLOT,
            // TokenType.TBOOL, TokenType.TIDEN)));
            tokenOutput.feedParserError(String.format("Syntax error - Missing %s (line %d, column %d) ",
                    "Struct Identifier", tokenList.peek().getLine(), tokenList.peek().getColumn()));
            // throw new RuntimeException("Critical error, expected a struct identifier");
        }
        return Declaration.structOfType(record);

    }
    // endregion

    // <stype> ::= int | float | bool
    private Declaration stype() {
        safePeek("Type", TokenType.TINTG, TokenType.TFLOT, TokenType.TBOOL);
        if (unrecoverable) {
            throw new RuntimeException("Critical error, expected a type");
        }
        Token token = tokenList.pop();
        TokenType tokenType = token.getType();
        if (tokenType == TokenType.TINTG) {
            return Declaration.INT;
        } else if (tokenType == TokenType.TFLOT) {
            return Declaration.FLOAT;
        } else if (tokenType == TokenType.TBOOL) {
            return Declaration.BOOL;
        }
        throw new RuntimeException("Unreachable code reached");
    }

    // <stats> ::= <stat>; <statstail> | <strstat> <statstail>
    private SyntaxTreeNode stats() {
        return statstail(true);
    }

    // <statstail> ::= <stat>; <statstail> | <strstat> <statstail> | ε
    private SyntaxTreeNode statstail(boolean forbidEpsilon) {
        boolean strstatOrstat = typeAtPeek(TokenType.TTFOR, TokenType.TIFTH, TokenType.TSWTH, TokenType.TTTDO,
                TokenType.TREPT, TokenType.TIDEN, TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN, TokenType.TRETN);
        if (strstatOrstat && forbidEpsilon) {
            safePeek("Statement", TokenType.TTFOR, TokenType.TIFTH, TokenType.TSWTH, TokenType.TTTDO, TokenType.TREPT,
                    TokenType.TIDEN, TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN, TokenType.TRETN);
            if (unrecoverable) {
                return getErrorNode();
            }
        }

        if (!strstatOrstat && !forbidEpsilon) {
            return null;
        }

        SyntaxTreeNode firstNode;
        // stat
        if (typeAtPeek(TokenType.TREPT, TokenType.TIDEN, TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN,
                TokenType.TRETN)) {
            firstNode = stat();
            safePeek(";", TokenType.TSEMI);
            if (unrecoverable) {
                return getErrorNode();
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
        safePeek("Statement", TokenType.TTFOR, TokenType.TIFTH, TokenType.TSWTH, TokenType.TTTDO);
        if (unrecoverable) {
            return getErrorNode();
        }

        if (typeAtPeek(TokenType.TTFOR)) {
            return forstat();
        }
        if (typeAtPeek(TokenType.TIFTH)) {
            return ifstat();
        }
        if (typeAtPeek(TokenType.TSWTH)) {
            return switchstat();
        }
        return dostat();
    }

    // <stat> ::= <repstat> | <iostat> | <returnstat> | <asgnstatorcallstat>
    private SyntaxTreeNode stat() {
        safePeek("Statement", TokenType.TREPT, TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN, TokenType.TRETN,
                TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        if (typeAtPeek(TokenType.TREPT)) {
            return repstat();
        }
        if (typeAtPeek(TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN)) {
            return iostat();
        }
        if (typeAtPeek(TokenType.TRETN)) {
            return returnstat();
        }
        return asgnstatorcallstat();
    }

    // <asgnstatorcallstat> ::= <id> <asgnstatorcallstattail>
    private SyntaxTreeNode asgnstatorcallstat() {
        safePeek("Identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        return asgnstatorcallstattail(idToken);
    }

    // <asgnstatorcallstattail> ::= <vartail> <asgnstattail> | ( <callstattail>
    private SyntaxTreeNode asgnstatorcallstattail(Token idToken) {
        if (typeAtPeek(TokenType.TLPAR)) {
            tokenList.pop(); // (
            // TLPAR
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
        // asgnstat branch
        SyntaxTreeNode varNode;
        if (typeAtPeek(TokenType.TLBRK)) {
            varNode = vartail(idToken);
        } else {
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
            varNode = new SyntaxTreeNode(TreeNodeType.NSIMV, idToken, record);
        }
        return asgnstattail(varNode);
    }

    // <forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
    private SyntaxTreeNode forstat() {
        safePeek("for", TokenType.TTFOR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // for

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        SyntaxTreeNode asgnlistNode = asgnlist();

        safePeek(";", TokenType.TSEMI);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // ;

        SyntaxTreeNode boolNode = bool();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        SyntaxTreeNode statsNode = stats();

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
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
        safePeek("repeat", TokenType.TREPT);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // repeat

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        SyntaxTreeNode asgnlistNode = asgnlist();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        SyntaxTreeNode statsNode = stats();

        safePeek("until", TokenType.TUNTL);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // until

        SyntaxTreeNode boolNode = bool();

        SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NREPT);
        result.setFirstChild(asgnlistNode);
        result.setSecondChild(statsNode);
        result.setThirdChild(boolNode);
        return result;
    }

    // <dostat> ::= do <stats> while ( <bool> ) end
    private SyntaxTreeNode dostat() {

        safePeek("do", TokenType.TTTDO);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // do

        SyntaxTreeNode statsNode = stats();

        safePeek("while", TokenType.TWHIL);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // while

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        SyntaxTreeNode boolNode = bool();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // end

        SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NDOWL);
        result.setFirstChild(statsNode);
        result.setThirdChild(boolNode);

        return result;
    }

    // <asgnlist> ::= <alist> | ε
    private SyntaxTreeNode asgnlist() {
        if (notTypeAtPeek(TokenType.TIDEN)) {
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
        if (notTypeAtPeek(TokenType.TCOMA)) {
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
        safePeek("if", TokenType.TIFTH);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // if

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        SyntaxTreeNode boolNode = bool();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        SyntaxTreeNode statsNode = stats();

        SyntaxTreeNode tail = ifstattail();
        // tail can either be null, stats (else) or an ifstat (elif)

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
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

            safePeek("(", TokenType.TLPAR);
            if (unrecoverable) {
                return getErrorNode();
            }

            tokenList.pop(); // (

            SyntaxTreeNode boolNode = bool();

            safePeek(")", TokenType.TRPAR);
            if (unrecoverable) {
                return getErrorNode();
            }

            tokenList.pop(); // )

            SyntaxTreeNode statsNode = stats();

            SyntaxTreeNode result = new SyntaxTreeNode(TreeNodeType.NIFTH);
            result.setFirstChild(boolNode);
            result.setThirdChild(statsNode);

            return result;
        }
        // ε
        return null;
    }

    // <switchstat> ::= switch ( <expr> ) begin <caselist> end
    private SyntaxTreeNode switchstat() {

        safePeek("switch", TokenType.TSWTH);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // switch

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        SyntaxTreeNode exprNode = expr();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        safePeek("begin", TokenType.TBEGN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // begin

        SyntaxTreeNode caselistNode = caselist();

        safePeek("end", TokenType.TTEND);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // end

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NSWTCH);

        node.setFirstChild(exprNode);
        node.setThirdChild(caselistNode);

        return node;
    }

    // <caselist> ::= case <expr> : <stats> break ; <caselist> | default : <stats>
    private SyntaxTreeNode caselist() {
        safePeek("case or default", TokenType.TCASE, TokenType.TDFLT);
        if (unrecoverable) {
            return getErrorNode();
        }

        if (typeAtPeek(TokenType.TCASE)) {

            tokenList.pop(); // case

            SyntaxTreeNode exprNode = expr();

            safePeek(":", TokenType.TCOLN);
            if (unrecoverable) {
                return getErrorNode();
            }

            tokenList.pop(); // :

            SyntaxTreeNode statsNode = stats();

            safePeek("break", TokenType.TBREK);
            if (unrecoverable) {
                return getErrorNode();
            }

            tokenList.pop(); // break

            safePeek(";", TokenType.TSEMI);
            if (unrecoverable) {
                return getErrorNode();
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

        safePeek(":", TokenType.TCOLN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // :

        SyntaxTreeNode statsNode = stats();

        // TODO: Not sure if this is the intended structure for default case
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NCASLT);
        node.setSecondChild(statsNode);
        return node;
    }

    // <asgnstat> ::= <var> <asgnstattail>
    private SyntaxTreeNode asgnstat() {
        SyntaxTreeNode varNode = var();
        return asgnstattail(varNode);
    }

    private SyntaxTreeNode asgnstattail(SyntaxTreeNode varNode) {
        TreeNodeType asgnopNode;
        try {
            asgnopNode = asgnop();
        } catch (RuntimeException e) {
            return new SyntaxTreeNode(TreeNodeType.NUNDEF);
        }
        SyntaxTreeNode boolNode = bool();

        SyntaxTreeNode node = new SyntaxTreeNode(asgnopNode);

        node.setFirstChild(varNode);
        node.setThirdChild(boolNode);

        return node;
    }

    // <asgnop> :: == | += | -= | *= | /=
    private TreeNodeType asgnop() {
        safePeek("Assignment Operator", TokenType.TEQUL, TokenType.TPLEQ, TokenType.TMNEQ, TokenType.TSTEQ,
                TokenType.TDVEQ);
        if (unrecoverable) {
            throw new RuntimeException("Critical error, expected an assignment operator");
        }

        TokenType tokenType = tokenList.pop().getType();

        if (tokenType == TokenType.TEQUL) {
            return TreeNodeType.NASGN;
        }
        if (tokenType == TokenType.TPLEQ) {
            return TreeNodeType.NPLEQ;
        }
        if (tokenType == TokenType.TMNEQ) {
            return TreeNodeType.NMNEQ;
        }
        if (tokenType == TokenType.TSTEQ) {
            return TreeNodeType.NSTEA;
        }
        // if (tokenType == TokenType.TDVEQ)
        return TreeNodeType.NDVEQ;
    }

    // <iostat> ::= input <vlist> | print <prlist> | printline <prlist>
    private SyntaxTreeNode iostat() {
        safePeek("input, print, or printline", TokenType.TINPT, TokenType.TPRNT, TokenType.TPRLN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token peekedToken = tokenList.peek();
        tokenList.pop(); // input, print, or printline
        if (peekedToken.getType() == TokenType.TINPT) {
            SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NINPUT);
            SyntaxTreeNode vlistNode = vlist();
            node.setFirstChild(vlistNode);
            return node;
        }

        SyntaxTreeNode node = new SyntaxTreeNode(
                peekedToken.getType() == TokenType.TPRNT ? TreeNodeType.NPRINT : TreeNodeType.NPRLN);

        SyntaxTreeNode prlistNode = prlist();

        node.setFirstChild(prlistNode);

        return node;
    }

    // So we never actually end up using this rule since we changed the grammar for
    // stat to asgnstatorcallstat which handles it
    // <callstat> ::= <id> ( <callstattail>
    private SyntaxTreeNode callstat() {
        safePeek("identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
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

        if (typeAtPeek(TokenType.TRPAR)) {

            tokenList.pop(); // )
            return null;
        }

        SyntaxTreeNode elistNode = elist();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        return elistNode;
    }

    // Note: This is easier to implement as a single function for both production
    // rules
    // <returnstat> ::= return <returnstattail>
    // <returnstattail> ::= void | <expr>
    private SyntaxTreeNode returnstat() {
        safePeek("return", TokenType.TRETN);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // return

        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NRETN);
        if (typeAtPeek(TokenType.TVOID)) {
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
        if (notTypeAtPeek(TokenType.TCOMA)) {
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
        safePeek("identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();
        return vartail(idToken);
    }

    // <vartail> ::= [<expr>]<vartailtail> | ε
    private SyntaxTreeNode vartail(Token idToken) {
        if (typeAtPeek(TokenType.TLBRK)) {
            tokenList.pop(); // [

            SyntaxTreeNode exprNode = expr();

            safePeek("]", TokenType.TRBRK);
            if (unrecoverable) {
                return getErrorNode();
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
        if (typeAtPeek(TokenType.TDOTT)) {

            tokenList.pop(); // .

            safePeek("identifier", TokenType.TIDEN);
            if (unrecoverable) {
                return getErrorNode();
            }

            Token endIdToken = tokenList.pop();

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
            currentSymbolTable = record.getScope();
            SymbolTableRecord endRecord = currentSymbolTable.getOrCreateToken(endIdToken.getLexeme(), endIdToken);
            currentSymbolTable = currentSymbolTable.getParent();

            // TODO: Not completely sure what children of this node should be? We
            // technically have two ids?
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

    // NEXPL <elist> ::= <bool> , <elist>
    // Special <elist> ::= <bool>

    // <elist> ::= <bool> <elisttail>
    private SyntaxTreeNode elist() {
        SyntaxTreeNode boolNode = bool();
        SyntaxTreeNode elisttailNode = elisttail();
        if (elisttailNode == null) {
            // Special <elist> ::= <bool>
            return boolNode;
        } else {
            // NEXPL <elist> ::= <bool> , <elist>
            elisttailNode.setFirstChild(boolNode);
            return elisttailNode;
        }

    }

    // <elisttail> ::= , <elist> | ε
    private SyntaxTreeNode elisttail() {
        if (typeAtPeek(TokenType.TCOMA)) {

            SymbolTableRecord elistRecord = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            SyntaxTreeNode elistNode = new SyntaxTreeNode(
                    TreeNodeType.NEXPL,
                    tokenList.pop(),
                    elistRecord);

            elistNode.setThirdChild(elist());
            return elistNode;

        } else {
            // Eee
            return null;
        }
    }

    // NBOOL <bool> ::= not <bool> | <bool> <logop> <rel>
    // Special <bool> ::= <rel>

    // <bool> ::= not <bool> | <rel> <boolPrime>
    private SyntaxTreeNode bool() {
        if (typeAtPeek(TokenType.TNOTT)) {

            tokenList.pop(); // not

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());
            SyntaxTreeNode boolNode = new SyntaxTreeNode(
                    TreeNodeType.NBOOL,
                    tokenList.pop(),
                    record);
            boolNode.setFirstChild(bool());
            return boolNode;

        } else {

            SyntaxTreeNode relNode = rel();
            return boolPrime(relNode);
        }
    }

    // <boolPrime> ::= <logop> <rel> <boolPrime> | ε
    private SyntaxTreeNode boolPrime(SyntaxTreeNode leftNode) {
        if (typeAtPeek(TokenType.TTAND) ||
                typeAtPeek(TokenType.TTTOR) ||
                typeAtPeek(TokenType.TTXOR)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            if (typeAtPeek(TokenType.TTAND)) {
                SyntaxTreeNode andNode = new SyntaxTreeNode(
                        TreeNodeType.NAND,
                        tokenList.pop(),
                        record);

                andNode.setFirstChild(leftNode);
                andNode.setThirdChild(logop());
                SyntaxTreeNode boolPrimeNode = boolPrime(andNode);
                return boolPrimeNode;
            } else if (typeAtPeek(TokenType.TTTOR)) {
                SyntaxTreeNode orNode = new SyntaxTreeNode(
                        TreeNodeType.NOR,
                        tokenList.pop(),
                        record);

                orNode.setFirstChild(leftNode);
                orNode.setThirdChild(logop());
                SyntaxTreeNode boolPrimeNode = boolPrime(orNode);
                return boolPrimeNode;
            } else {
                SyntaxTreeNode xorNode = new SyntaxTreeNode(
                        TreeNodeType.NXOR,
                        tokenList.pop(),
                        record);

                xorNode.setFirstChild(leftNode);
                xorNode.setThirdChild(logop());
                SyntaxTreeNode boolPrimeNode = boolPrime(xorNode);
                return boolPrimeNode;
            }

        } else {
            return leftNode;
        }
    }

    // Special <rel> ::= <expr> <relop><expr>
    // Special <rel> ::= <expr>

    // <rel> ::= <expr> <reltail>
    private SyntaxTreeNode rel() {

        SyntaxTreeNode relNode = expr();
        return reltail(relNode);
    }

    // <reltail> ::= <relop><expr> | ε
    private SyntaxTreeNode reltail(SyntaxTreeNode leftNode) {

        if (typeAtPeek(TokenType.TEQEQ) ||
                typeAtPeek(TokenType.TNEQL) ||
                typeAtPeek(TokenType.TGRTR) ||
                typeAtPeek(TokenType.TLEQL) ||
                typeAtPeek(TokenType.TLESS) ||
                typeAtPeek(TokenType.TGEQL)) {

            SyntaxTreeNode relopNode = relop();
            relopNode.setFirstChild(leftNode);

            relopNode.setThirdChild(expr());
            return relopNode;

        } else {
            return leftNode;
            // Eeeeee ee eee eee eee
        }
    }

    // NAND <logop> ::= and
    // NOR <logop> ::= or
    // NXOR <logop> ::= xor

    // <logop> ::= and | or | xor
    private SyntaxTreeNode logop() {
        safePeek("Logical Operator", TokenType.TTAND, TokenType.TTTOR, TokenType.TTXOR);
        if (unrecoverable) {
            return getErrorNode();
        }
        if (typeAtPeek(TokenType.TTAND)) {

            // and
            SyntaxTreeNode andNode = new SyntaxTreeNode(
                    TreeNodeType.NAND,
                    tokenList.pop(),
                    currentSymbolTable.getOrCreateToken(
                            tokenList.peek().getLexeme(),
                            tokenList.peek()));
            return andNode;
        } else if (typeAtPeek(TokenType.TTTOR)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // or
            SyntaxTreeNode orNode = new SyntaxTreeNode(
                    TreeNodeType.NOR,
                    tokenList.pop(),
                    record);
            return orNode;

        }
        // else if (typeAtPeek(TokenType.TTXOR))
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                tokenList.peek().getLexeme(),
                tokenList.peek());

        // xor
        SyntaxTreeNode xorNode = new SyntaxTreeNode(
                TreeNodeType.NXOR,
                tokenList.pop(),
                record);
        return xorNode;
    }

    // NEQL <relop> ::= ==
    // NNEQ <relop> ::= !=
    // NGRT <relop> ::= >
    // NLSS <relop> ::= <
    // NLEQ <relop> ::= <=
    // NGEQ <relop> ::= >=

    // <relop> ::= == | != | > | <= | < | >=
    private SyntaxTreeNode relop() {
        safePeek("Relational Operator", TokenType.TEQEQ, TokenType.TNEQL, TokenType.TGRTR, TokenType.TLEQL,
                TokenType.TLESS, TokenType.TGEQL);
        if (unrecoverable) {
            return getErrorNode();
        }
        if (typeAtPeek(TokenType.TEQEQ)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // ==
            SyntaxTreeNode equalEqualNode = new SyntaxTreeNode(
                    TreeNodeType.NEQL,
                    tokenList.pop(),
                    record);
            return equalEqualNode;

        } else if (typeAtPeek(TokenType.TNEQL)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // !=
            SyntaxTreeNode notEqualNode = new SyntaxTreeNode(
                    TreeNodeType.NNEQ,
                    tokenList.pop(),
                    record);
            return notEqualNode;

        } else if (typeAtPeek(TokenType.TGRTR)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // >
            SyntaxTreeNode greaterThanNode = new SyntaxTreeNode(
                    TreeNodeType.NGRT,
                    tokenList.pop(),
                    record);
            return greaterThanNode;

        } else if (typeAtPeek(TokenType.TLEQL)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // <=
            SyntaxTreeNode lessThanEqualNode = new SyntaxTreeNode(
                    TreeNodeType.NLEQ,
                    tokenList.pop(),
                    record);
            return lessThanEqualNode;

        } else if (typeAtPeek(TokenType.TLESS)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // <
            SyntaxTreeNode lessThanNode = new SyntaxTreeNode(
                    TreeNodeType.NLSS,
                    tokenList.pop(),
                    record);
            return lessThanNode;

            // } else if (typeAtPeek(TokenType.TGEQL)) {
        }
        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                tokenList.peek().getLexeme(),
                tokenList.peek());

        // >=
        SyntaxTreeNode greaterThanEqualNode = new SyntaxTreeNode(
                TreeNodeType.NGEQ,
                tokenList.pop(),
                record);
        return greaterThanEqualNode;
    }

    // <expr> ::= <term> <exprtail>
    private SyntaxTreeNode expr() {
        SyntaxTreeNode termnode = term();
        SyntaxTreeNode exprNode = exprtail();
        if (exprNode == null) {
            // Special <expr> ::= <term>
            return termnode;
        }
        // NADD <expr> ::= <expr> + <term>
        // NSUB <expr> ::= <expr> - <term>
        exprNode.setThirdChild(termnode);
        return exprNode;
    }

    // <exprtail> ::= + <expr> | - <expr> | ε
    private SyntaxTreeNode exprtail() {
        if (typeAtPeek(TokenType.TPLUS) || typeAtPeek(TokenType.TMINS)) {

            Token expToken = tokenList.pop(); // + or -

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(expToken.getLexeme(), expToken);
            SyntaxTreeNode node = new SyntaxTreeNode(
                    expToken.getType() == TokenType.TPLUS ? TreeNodeType.NADD : TreeNodeType.NSUB, expToken, record);

            node.setFirstChild(expr());
            return node;

        } else {
            // epslon
            return null;
        }
    }

    // NMUL <term> ::= <term> * <fact>
    // NDIV <term> ::= <term> / <fact>
    // NMOD <term> ::= <term> % <fact>

    // <term> ::= <fact> <termtail>
    private SyntaxTreeNode term() {
        SyntaxTreeNode factNode = fact();
        SyntaxTreeNode termNode = termtail();
        if (termNode == null) {
            // Special <term> ::= <fact>
            return factNode;
        }
        // NMUL <term> ::= <term> * <fact>
        // NDIV <term> ::= <term> / <fact>
        // NMOD <term> ::= <term> % <fact>
        termNode.setThirdChild(factNode);
        return termNode;
    }

    // <termtail> ::= * <term> | / <term> | % <term> | ε
    private SyntaxTreeNode termtail() {
        if (typeAtPeek(TokenType.TSTAR) ||
                typeAtPeek(TokenType.TDIVD) ||
                typeAtPeek(TokenType.TPERC)) {

            Token expToken = tokenList.pop(); // * or / or %
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(expToken.getLexeme(), expToken);

            SyntaxTreeNode node = new SyntaxTreeNode(
                    expToken.getType() == TokenType.TSTAR ? TreeNodeType.NMUL
                            : expToken.getType() == TokenType.TDIVD ? TreeNodeType.NDIV : TreeNodeType.NMOD,
                    expToken,
                    record);

            node.setFirstChild(term());
            return node;

        } else {
            // epslon
            return null;
        }
    }

    // NPOW <fact> ::= <fact> ^ <exponent>
    // Special <fact> ::= <exponent>

    // <fact> ::= <exponent> <factPrime>
    private SyntaxTreeNode fact() {
        SyntaxTreeNode expNode = exponent();
        SyntaxTreeNode factPrimeNode = factPrime();
        if (factPrimeNode == null) {
            // Special <fact> ::= <exponent>
            return expNode;
        }
        // NPOW <fact> ::= <fact> ^ <exponent>
        factPrimeNode.setThirdChild(expNode);
        return factPrimeNode;
    }

    // <factPrime> ::= ^ <exponent> <factPrime> | ε
    private SyntaxTreeNode factPrime() {
        if (typeAtPeek(TokenType.TCART)) {

            Token expToken = tokenList.pop(); // ^
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(expToken.getLexeme(), expToken);
            SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NPOW, expToken, record);
            node.setFirstChild(exponent());
            node.setSecondChild(factPrime());
            return node;

        } else {
            // epslon
            return null;
        }
    }

    // Special <exponent> ::= <var>
    // NILIT <exponent> ::= <intlit>
    // NFLIT <exponent> ::= <reallit>
    // Special <exponent> ::= <fncall>
    // NTRUE <exponent> ::= true
    // NFALS <exponent> ::= false
    // Special <exponent> ::= ( <bool> )

    // <exponent> ::= <exponentNotBool> | <exponentBool>
    private SyntaxTreeNode exponent() {
        if (typeAtPeek(TokenType.TLPAR)) {

            // <exponent> ::= ( <bool> )
            SyntaxTreeNode expBoolNode = exponentBool();
            return expBoolNode;

        } else {

            SyntaxTreeNode expNotBoolNode = exponentNotBool();
            return expNotBoolNode;

        }
    }

    // <exponentNotBool> ::= <var> | <intlit> | <reallit> | <fncall> | true | false
    private SyntaxTreeNode exponentNotBool() {
        if (typeAtPeek(TokenType.TIDEN)) {
            // Special <exponent> ::= <var>

            SyntaxTreeNode varNode = var();
            return varNode;

        } else if (typeAtPeek(TokenType.TILIT)) {
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // NILIT <exponent> ::= <intlit>
            SyntaxTreeNode intNode = new SyntaxTreeNode(
                    TreeNodeType.NILIT,
                    tokenList.peek(),
                    record);

            tokenList.pop(); // intlit (int)

            return intNode;

        } else if (typeAtPeek(TokenType.TFLIT)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());
            // NFLIT <exponent> ::= <reallit>
            SyntaxTreeNode floatNode = new SyntaxTreeNode(
                    TreeNodeType.NFLIT,
                    tokenList.peek(),
                    record);

            tokenList.pop(); // reallit (float)

            return floatNode;

        } else if (typeAtPeek(TokenType.TTRUE) || typeAtPeek(TokenType.TFALS)) {

            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());

            // NTRUE <exponent> ::= true
            // NFALS <exponent> ::= false
            SyntaxTreeNode boolNode = new SyntaxTreeNode(
                    tokenList.peek().getType() == TokenType.TTRUE ? TreeNodeType.NTRUE : TreeNodeType.NFALS,
                    tokenList.peek(),
                    record);

            tokenList.pop(); // true or false

            return boolNode;

        } else {
            // Special <exponent> ::= <fncall>
            SyntaxTreeNode fncallNode = fncall();
            return fncallNode;

        }
    }

    // <exponentBool> ::= ( <bool> )
    public SyntaxTreeNode exponentBool() {
        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        SyntaxTreeNode boolNode = bool();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // )

        return boolNode;

    }

    // <fncall> ::= <id> ( <fncalltail>
    private SyntaxTreeNode fncall() {
        safePeek("identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
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
        if (typeAtPeek(TokenType.TRPAR)) {
            tokenList.pop(); // )
            return null;
        }

        SyntaxTreeNode elistNode = elist();

        safePeek(")", TokenType.TRPAR);
        if (unrecoverable) {
            return getErrorNode();
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

        if (notTypeAtPeek(TokenType.TCOMA)) {
            return null;
        }

        tokenList.pop(); // ,

        // TODO: This might be increadibly incorrect!
        return prlist();

    }

    // <printitem> ::= <expr> | <string>
    private SyntaxTreeNode printitem() {

        if (typeAtPeek(TokenType.TSTRG)) {
            Token token = tokenList.pop();
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(token.getLexeme(), token);
            // TODO: Setting declaration to string might be needed!;
            return new SyntaxTreeNode(TreeNodeType.NSTRG, token, record);
        }
        return expr();
    }

    private boolean popTillTokenType(TokenType... types) {
        if (tokenList.isEmpty()) {
            // true means unrecoverable
            return true;
        }
        while (!typeAtPeek(types)) {
            tokenList.pop();
            if (tokenList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
