import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

        typeCheck(node);

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
        if (typeAtPeek(TokenType.TCONS, TokenType.TTYPD, TokenType.TARRD)) {
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

        if (currentSymbolTable.getToken(idToken.getLexeme()).isPresent()) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Identifier %s already declared (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
        }

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

        if (currentSymbolTable.getToken(idToken.getLexeme()).isPresent()) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Identifier %s already declared (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
        }

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

    // #region <fieldstail> ::= , <sdecl> <fieldstail> | ε
    private SyntaxTreeNode fieldstail() {
        if (notTypeAtPeek(TokenType.TCOMA)) {
            // this is an epsilon production
            return null;
        }
        tokenList.pop(); // ,
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
        if (currentSymbolTable.getToken(idToken.getLexeme()).isPresent()) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Identifier %s already declared (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
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

        if (currentSymbolTable.getToken(idToken.getLexeme()).isPresent()) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Identifier %s already declared (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
        }

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
        if (typeRecord.getDeclaration().isPresent() && !typeRecord.getDeclaration().get().equals(Declaration.ARRAY_TYPE)) {
            tokenOutput.feedSemanticError(String.format(
                    "Semantic error - Array declaration type does not match type identifier (line %d, column %d) ",
                    typeIdToke.getLine(), typeIdToke.getColumn()));
        }
        record.setDeclaration(Declaration.arrayOfType(typeRecord));
        SyntaxTreeNode node = new SyntaxTreeNode(TreeNodeType.NARRD, idToken, record);
        return node;
    }
    // endregion

    // #region <funcs> ::= <func> <funcs> | ε
    private SyntaxTreeNode funcs() {
        if (notTypeAtPeek(TokenType.TFUNC) || tokenList.isEmpty()) {
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

        if (currentSymbolTable.getToken(idToken.getLexeme()).isPresent()) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Identifier %s already declared (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
        }

        SymbolTableRecord record = currentSymbolTable.getOrCreateToken(idToken.getLexeme(), idToken);
        record.setDeclaration(Declaration.FUNCTION);

        safePeek("(", TokenType.TLPAR);
        if (unrecoverable) {
            return getErrorNode();
        }

        tokenList.pop(); // (

        currentSymbolTable = record.getScope();

        SyntaxTreeNode plistNode = plist();

        if (plistNode != null) {
            // append to arguments of function symbol table record
            SyntaxTreeNode current = plistNode.getFirstChild().orElse(null);
            while (current != null) {
                SymbolTableRecord argument = current.getValueRecord().get();
                record.addArgument(argument);
                current = current.getFirstChild().orElse(null);
            }
        }

        currentSymbolTable = currentSymbolTable.getParent();

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

        // Traverse stats to ensure a return statement is present
        Queue<SyntaxTreeNode> queue = new LinkedList<>();
        queue.add(stats);
        boolean hasReturn = false;
        while (!queue.isEmpty()) {
            SyntaxTreeNode current = queue.poll();
            if (current.getNodeType() == TreeNodeType.NRETN) {
                hasReturn = true;
                break;
            }
            // check all of the present children
            if (current.getFirstChild().isPresent()) {
                queue.add(current.getFirstChild().get());
            }
            if (current.getSecondChild().isPresent()) {
                queue.add(current.getSecondChild().get());
            }
            if (current.getThirdChild().isPresent()) {
                queue.add(current.getThirdChild().get());
            }
        }
        if (!hasReturn) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Function %s does not have a return statement (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
        }

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
        if (!typeAtPeek(TokenType.TIDEN)) {
            return stypeOrStructid();
        }

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
                    new SyntaxTreeNode(TreeNodeType.NUNDEF) 
            };
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
        if (currentSymbolTable.getToken(idToken.getLexeme()).isPresent()) {
            tokenOutput.feedSemanticError(
                    String.format("Semantic error - Identifier %s already declared (line %d, column %d) ",
                            idToken.getLexeme(), idToken.getLine(), idToken.getColumn()));
        }

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
            tokenOutput.feedSemanticError(String.format(
                    "Semantic error - Main end identifier does not match program identifier (line %d, column %d) ",
                    idToken.getLine(), idToken.getColumn()));
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
        node.setFirstChild(statsNode);
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

        // TODO: Check that the types are compatible

        node.setFirstChild(varNode);
        node.setThirdChild(boolNode);

        return node;
    }

    // <asgnop> :: = | += | -= | *= | /=
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
        if (idToken.getLexeme().equals("funny")) {
            throw new RuntimeException("funny");
        }

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
            
            SyntaxTreeNode logopNode = logop();
            logopNode.setFirstChild(leftNode);
            logopNode.setThirdChild(rel());
            SyntaxTreeNode boolPrimeNode = boolPrime(logopNode);
            return boolPrimeNode;
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
        tokenList.pop(); // and, or, xor
        if (typeAtPeek(TokenType.TTAND)) {

            // and
            SyntaxTreeNode andNode = new SyntaxTreeNode(TreeNodeType.NAND);
            return andNode;
        } else if (typeAtPeek(TokenType.TTTOR)) {
            // or
            SyntaxTreeNode orNode = new SyntaxTreeNode(TreeNodeType.NOR);
            return orNode;

        }
        // else if (typeAtPeek(TokenType.TTXOR))
        // xor
        SyntaxTreeNode xorNode = new SyntaxTreeNode(TreeNodeType.NXOR);
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

    // <exponentNotBool> ::= <intlit> | <reallit> | true | false | <varOrFncall>
    private SyntaxTreeNode exponentNotBool() {
        if (typeAtPeek(TokenType.TIDEN)) {
            // Special <exponent> ::= <var>
            return varOrFncall();
        } else if (typeAtPeek(TokenType.TILIT)) {
            SymbolTableRecord record = currentSymbolTable.getOrCreateToken(
                    tokenList.peek().getLexeme(),
                    tokenList.peek());
            record.setDeclaration(Declaration.INT);

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

        }
        throw new RuntimeException("Critical error, unreachable code");
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

    // <varOrFncall> ::= <id> <varOrFncalltail>
    public SyntaxTreeNode varOrFncall() {
        safePeek("identifier", TokenType.TIDEN);
        if (unrecoverable) {
            return getErrorNode();
        }

        Token idToken = tokenList.pop();

        return varOrFncalltail(idToken);
    }

    // <varOrFncalltail>::= ( <fncalltail> | <vartail>
    public SyntaxTreeNode varOrFncalltail(Token idToken) {
        if (typeAtPeek(TokenType.TLPAR)) {
            // <varOrFncalltail> ::= ( <fncalltail>
            return fncall(idToken);
        }
        // <varOrFncalltail> ::= <vartail>
        return vartail(idToken);
    }

    // <fncall> ::= ( <fncalltail>
    private SyntaxTreeNode fncall(Token idToken) {
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
            if (record.getArguments().isEmpty()) {
                // Wrong amount of arguments
                tokenOutput.feedSemanticError(
                        String.format("Semantic error - wrong amount of arguments for function (line %d, column %d) ",
                                idToken.getLine(), idToken.getColumn()));
            } else {
                List<SymbolTableRecord> arguments = record.getArguments().get();
                SyntaxTreeNode current = elistNode;
                for (SymbolTableRecord argument : arguments) {
                    if (current == null) {
                        // Wrong amount of arguments
                        tokenOutput.feedSemanticError(String.format(
                                "Semantic error - wrong amount of arguments for function (line %d, column %d) ",
                                idToken.getLine(), idToken.getColumn()));
                        break;
                    }
                    if (current.getValueRecord().isPresent()
                            && current.getValueRecord().get().getDeclaration().isPresent()) {
                        if (!current.getValueRecord().get().getDeclaration().get()
                                .equals(argument.getDeclaration().get())) {
                            // Wrong type of argument
                            tokenOutput.feedSemanticError(String.format(
                                    "Semantic error - wrong type of argument for function (line %d, column %d) ",
                                    idToken.getLine(), idToken.getColumn()));
                        }
                    } else if (!current.getNodeType().equals(TreeNodeType.NSIMV)) {
                        Declaration resulting = getResultingTypeOf(current);
                        if (!resulting.equals(argument.getDeclaration().get())) {
                            tokenOutput.feedSemanticError(String.format(
                                    "Semantic error - wrong type of argument for function (line %d, column %d) ",
                                    idToken.getLine(), idToken.getColumn()));
                        }
                    } else {
                        // missing symbol table record / missing declaration
                        tokenOutput.feedSemanticError(String.format(
                                "Semantic error - wrong amount of arguments for function (line %d, column %d) ",
                                idToken.getLine(), idToken.getColumn()));
                    }
                    if (current.getFirstChild().isPresent()) {
                        current = current.getFirstChild().get();
                    } else {
                        current = null;
                    }
                }
            }
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SEMANTIC ANALYSIS TINGS
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // #region SEMANTIC ANALYSIS TINGS

    private boolean typeIsOperator(TreeNodeType type) {
        return type == TreeNodeType.NEQL ||
                type == TreeNodeType.NNEQ ||
                type == TreeNodeType.NGRT ||
                type == TreeNodeType.NLSS ||
                type == TreeNodeType.NLEQ ||
                type == TreeNodeType.NGEQ ||
                type == TreeNodeType.NADD ||
                type == TreeNodeType.NSUB ||
                type == TreeNodeType.NMUL ||
                type == TreeNodeType.NDIV ||
                type == TreeNodeType.NMOD ||
                type == TreeNodeType.NPOW;
    }

    private boolean typeIsBooleanOperator(TreeNodeType type) {
        return type == TreeNodeType.NAND ||
                type == TreeNodeType.NOR ||
                type == TreeNodeType.NXOR;
    }

    private boolean isNumeric(Declaration type) {
        return type.equals(Declaration.INT) ||
                type.equals(Declaration.FLOAT);
    }

    // pre order travesal only as it sets checked, the idea of this is to check it
    // at the highest point and prevent the same operator from being checked
    // multiple times
    // when moving forn the pre order ;)
    private void typeCheck(SyntaxTreeNode node) {
        if (node == null) {
            return;
        }

        var type = node.getNodeType();

        // Arithmitic operators
        if (typeIsOperator(type)) {
            recursiveOperatorTypeCheck(node);
        }
        // bool operators
        else if (typeIsBooleanOperator(type)) {
            recursiveBooleanOperatorTypeCheck(node);
        } else if (type == TreeNodeType.NINIT) {
            // TODO: Check if the type of the left and right are the same without the other
            // shit because this is an assignment
            // i think this has to do with the real variables and real arrays
        }

        return;

    }

    // basically it starts at the first operator then recursively checks the
    // children to
    // ensure that they are the same type, if not a semantic error is thrown

    private SyntaxTreeNode recursiveOperatorTypeCheck(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        if (!node.getValueRecord().isPresent()) {
            return null;
        }

        if (node.getTypeChecked() == true) {
            return null;
        }

        SyntaxTreeNode nodeOne = node.getFirstChild().isPresent() ? recursiveTypeCheck(node.getFirstChild().get())
                : null;

        SyntaxTreeNode nodeTwo = node.getThirdChild().isPresent() ? recursiveTypeCheck(node.getThirdChild().get())
                : null;

        node.setTypeChecked(); // so that the pre order traversal does not re check an operator that has
                               // already been checked

        if (nodeOne == null || nodeTwo == null) {
            return null;
        }
        if (!nodeOne.getValueRecord().isPresent() || !nodeTwo.getValueRecord().isPresent()) {
            return null;
        }
        if (!nodeOne.getValueRecord().get().getDeclaration().isPresent()
                || !nodeTwo.getValueRecord().get().getDeclaration().isPresent()) {
            return null;
        }

        Declaration typeOne = nodeOne.getValueRecord().get().getDeclaration().get();
        Declaration typeTwo = nodeTwo.getValueRecord().get().getDeclaration().get();

        if (!typeOne.equals(typeTwo)) {
            // SEMANTIC ERROR
            int errorLine = node.getValueRecord().get().getOgToken().getLine();
            int errorColumn = node.getValueRecord().get().getOgToken().getColumn();
            String errorMessage = String.format(
                    "Semantic Error - Arithemic operations can only be done on 2 of the same types (line %d, column %d)",
                    errorLine,
                    errorColumn);
            tokenOutput.feedSemanticError(errorMessage);

            return null;
        }

        return nodeOne;

    }

    private SyntaxTreeNode recursiveTypeCheck(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        TreeNodeType type = node.getNodeType();
        TokenType tokenType = node.getValueRecord().isPresent() ? node.getValueRecord().get().getType() : null;

        if (typeIsOperator(type)) { // if it is an operator then recursive operator

            return recursiveOperatorTypeCheck(node);

        } else if (tokenType == TokenType.TIDEN) { // if it is an identifier then check then convert the type to token
                                                   // type

            Declaration decType = node.getValueRecord().get().getDeclaration().get();

            if (decType.equals(Declaration.INT) || decType.equals(Declaration.FLOAT) || (decType.equals(Declaration.FUNCTION) && node.getValueRecord().get().getReturnType().get().equals(Declaration.INT) || node.getValueRecord().get().getReturnType().get().equals(Declaration.FLOAT))) {
                return node;
            } else {
                // SEMANTIC ERROR CANNOT BE A WHATEVER IT IS
                // TODO: Since we are not storing the token of the actual operator we don't have
                // an accurate line and column
                int errorLine = node.getValueRecord().get().getOgToken().getLine();
                int errorColumn = node.getValueRecord().get().getOgToken().getColumn();
                String errorMessage = String.format(
                        "Semantic Error - Arithemic operations can only be done on numeric types (line %d, column %d)",
                        errorLine, errorColumn);
                tokenOutput.feedSemanticError(errorMessage);

                return null;
            }
        } else { // if bare then just return the type
            if (isNumeric(
                    (node.getValueRecord().isPresent() ? node.getValueRecord().get().getDeclaration().get()
                            : null))) {
                return node;
            }
            // SEMANTIC ERROR
            int errorLine = node.getValueRecord().get().getOgToken().getLine();
            int errorColumn = node.getValueRecord().get().getOgToken().getColumn();
            // TODO: Since we are not storing the token of the actual operator we don't have
            // an accurate line and column
            String errorMessage = String.format(
                    "Semantic Error - Arithemic operations can only be done on numeric types (line %d, column %d)",
                    errorLine, errorColumn);
            tokenOutput.feedSemanticError(errorMessage);

            return null;
        }
    }

    // same as above except for boolean operators
    private SyntaxTreeNode recursiveBooleanOperatorTypeCheck(SyntaxTreeNode node) {

        if (node == null) {
            return null;
        }

        if (node.getTypeChecked() == true) {
            return null;
        }

        SyntaxTreeNode nodeOne = node.getFirstChild().isPresent()
                ? recursiveBooleanTypeCheck(node.getFirstChild().get())
                : null;

        SyntaxTreeNode nodeTwo = node.getThirdChild().isPresent()
                ? recursiveBooleanTypeCheck(node.getThirdChild().get())
                : null;

        node.setTypeChecked(); // so that the pre order traversal does not re check an operator that has
                               // already been checked

        if (nodeOne == null || nodeTwo == null) {
            return null;
        }
        if (!nodeOne.getValueRecord().isPresent() || !nodeTwo.getValueRecord().isPresent()) {
            return null;
        }
        if (!nodeOne.getValueRecord().get().getDeclaration().isPresent()
                || !nodeTwo.getValueRecord().get().getDeclaration().isPresent()) {
            return null;
        }

        Declaration typeOne = nodeOne.getValueRecord().get().getDeclaration().get();
        Declaration typeTwo = nodeTwo.getValueRecord().get().getDeclaration().get();

        if (!typeOne.equals(Declaration.BOOL) || !typeTwo.equals(Declaration.BOOL)) {
            if (!typeOne.equals(Declaration.BOOL)) {
                // SEMANTIC ERROR

                var errorLine = node.getValueRecord().get().getOgToken().getLine();
                var errorColumn = node.getValueRecord().get().getOgToken().getColumn();
                String errorMessage = String.format(
                        "Semantic Error - Boolean operations can only be done on boolean types (line %d, column %d)",
                        errorLine, errorColumn);
                tokenOutput.feedSemanticError(errorMessage);
            } else if (!typeTwo.equals(Declaration.BOOL)) {
                // SEMANTIC ERROR

                var errorLine = node.getValueRecord().get().getOgToken().getLine();
                var errorColumn = node.getValueRecord().get().getOgToken().getColumn();
                String errorMessage = String.format(
                        "Semantic Error - Boolean operations can only be done on boolean types (line %d, column %d)",
                        errorLine, errorColumn);
                tokenOutput.feedSemanticError(errorMessage);
            }
            return null;
        }

        return node;

    }

    private SyntaxTreeNode recursiveBooleanTypeCheck(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        TreeNodeType type = node.getNodeType();
        TokenType tokenType = node.getValueRecord().isPresent() ? node.getValueRecord().get().getType() : null;

        if (typeIsBooleanOperator(type)) { // if it is an operator then recursive operator

            return recursiveBooleanOperatorTypeCheck(node);

        } else if (tokenType == TokenType.TIDEN) { // if it is an identifier then check then convert the type to token
                                                   // type

            var decType = node.getValueRecord().get().getDeclaration().get();

            if (decType.equals(Declaration.BOOL)) {
                return node;
            } else {
                // SEMANTIC ERROR CANNOT BE A WHATEVER IT IS

                var errorLine = node.getValueRecord().get().getOgToken().getLine();
                var errorColumn = node.getValueRecord().get().getOgToken().getColumn();
                String errorMessage = String.format(
                        "Semantic Error - Boolean operations can only be done on boolean types (line %d, column %d)",
                        errorLine, errorColumn);
                tokenOutput.feedSemanticError(errorMessage);

                return null;
            }
        } else { // if bare then just return the type
            return node;
        }
    }

    // #endregion

    
    
    /*
    *   // E.x. 1 - variable, need to determine the type of the expression
        // => Declaration.INTEGER
        public Declartion getResultingTypeOf(SyntaxTreeNode node) {
            if no children, u return current 
            otherwise you take the type of children
            and if we have int and float => float
            if we have int and int => int
        }
    */

    // returns null if the types are not the same (this will be caught by the semantic analysis later so probably no need to throw an error here? but the type will be null)
    private Declaration getResultingTypeOf(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        var type = node.getNodeType();

        Declaration returnType = null;

        // Arithmitic operators
        if (typeIsOperator(type)) {
            var result = recursiveOperatorTypeCheckDeclaration(node);
            if (result == null) {
                return null;
            }
            returnType = result.getValueRecord().get().getDeclaration().get();
        }
        // bool operators
        else if (typeIsBooleanOperator(type)) {
            var result = recursiveBooleanOperatorTypeCheckDeclaration(node);
            if (result == null) {
                return null;
            }
            returnType = result.getValueRecord().get().getDeclaration().get();
        } 

        return returnType;
    }

    private SyntaxTreeNode recursiveOperatorTypeCheckDeclaration(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        if (!node.getValueRecord().isPresent()) {
            return null;
        }

        SyntaxTreeNode nodeOne = node.getFirstChild().isPresent() ? recursiveTypeCheckDeclaration(node.getFirstChild().get())
                : null;

        SyntaxTreeNode nodeTwo = node.getThirdChild().isPresent() ? recursiveTypeCheckDeclaration(node.getThirdChild().get())
                : null;

        if (nodeOne == null || nodeTwo == null) {
            return null;
        }
        if (!nodeOne.getValueRecord().isPresent() || !nodeTwo.getValueRecord().isPresent()) {
            return null;
        }
        if (!nodeOne.getValueRecord().get().getDeclaration().isPresent()
                || !nodeTwo.getValueRecord().get().getDeclaration().isPresent()) {
            return null;
        }

        Declaration typeOne = nodeOne.getValueRecord().get().getDeclaration().get();
        Declaration typeTwo = nodeTwo.getValueRecord().get().getDeclaration().get();

        if (!typeOne.equals(typeTwo)) {

            return null;
        }

        return nodeOne;
    }


    private SyntaxTreeNode recursiveTypeCheckDeclaration(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        TreeNodeType type = node.getNodeType();
        TokenType tokenType = node.getValueRecord().isPresent() ? node.getValueRecord().get().getType() : null;

        if (typeIsOperator(type)) { // if it is an operator then recursive operator

            return recursiveOperatorTypeCheck(node);

        } else if (tokenType == TokenType.TIDEN) { // if it is an identifier then check then convert the type to token
                                                   // type

            Declaration decType = node.getValueRecord().get().getDeclaration().get();

            if (decType.equals(Declaration.INT) || decType.equals(Declaration.FLOAT)) {
                return node;
            } else {

                return null;
            }
        } else { // if bare then just return the type
            if (isNumeric(
                    (node.getValueRecord().isPresent() ? node.getValueRecord().get().getDeclaration().get()
                            : null))) {
                return node;
            }

            return null;
        }
    }

    // same as above except for boolean operators
    private SyntaxTreeNode recursiveBooleanOperatorTypeCheckDeclaration(SyntaxTreeNode node) {

        if (node == null) {
            return null;
        }

        SyntaxTreeNode nodeOne = node.getFirstChild().isPresent()
                ? recursiveBooleanTypeCheckDeclaration(node.getFirstChild().get())
                : null;

        SyntaxTreeNode nodeTwo = node.getThirdChild().isPresent()
                ? recursiveBooleanTypeCheckDeclaration(node.getThirdChild().get())
                : null;

        if (nodeOne == null || nodeTwo == null) {
            return null;
        }
        if (!nodeOne.getValueRecord().isPresent() || !nodeTwo.getValueRecord().isPresent()) {
            return null;
        }
        if (!nodeOne.getValueRecord().get().getDeclaration().isPresent()
                || !nodeTwo.getValueRecord().get().getDeclaration().isPresent()) {
            return null;
        }

        Declaration typeOne = nodeOne.getValueRecord().get().getDeclaration().get();
        Declaration typeTwo = nodeTwo.getValueRecord().get().getDeclaration().get();

        if (!typeOne.equals(Declaration.BOOL) || !typeTwo.equals(Declaration.BOOL)) {
            if (!typeOne.equals(Declaration.BOOL)) {
            } else if (!typeTwo.equals(Declaration.BOOL)) {
            }
            return null;
        }

        return nodeOne;

    }

    private SyntaxTreeNode recursiveBooleanTypeCheckDeclaration(SyntaxTreeNode node) {
        if (node == null) {
            return null;
        }

        TreeNodeType type = node.getNodeType();
        TokenType tokenType = node.getValueRecord().isPresent() ? node.getValueRecord().get().getType() : null;

        if (typeIsBooleanOperator(type)) { // if it is an operator then recursive operator

            return recursiveBooleanOperatorTypeCheck(node);

        } else if (tokenType == TokenType.TIDEN) { // if it is an identifier then check then convert the type to token
                                                   // type

            var decType = node.getValueRecord().get().getDeclaration().get();

            if (decType.equals(Declaration.BOOL)) {
                return node;
            } else {

                return null;
            }
        } else { // if bare then just return the type
            return node;
        }
    }



}
