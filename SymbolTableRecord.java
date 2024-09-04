public class SymbolTableRecord {
    private final SymbolTable parent;
    private final int id;
    private SymbolTable ownScope;
    private final TokenType type;
    private String name; // id/int/reals/strings
    private Token ogToken; // contains line info e.t.c of first declaration

    // for later use
    private int base;
    private int offset;

    public SymbolTableRecord(SymbolTable parent, TokenType type, String name, Token ogToken) {
        this.parent = parent;
        this.id = parent.getNextId();
        this.type = type;
        this.name = name;
        this.ogToken = ogToken;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public TokenType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Token getOgToken() {
        return ogToken;
    }

    // Later use
    public int getBase() {
        return base;
    }
    
    // Later use
    public int getOffset() {
        return offset;
    }

    public SymbolTable getScope() {
        // TODO: This should be restricted to function identifiers e.t.c.
        if (ownScope == null) {
            ownScope = new SymbolTable(parent);
        }
        return ownScope;
    }
}
