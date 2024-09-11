import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SymbolTableRecord {
    private final SymbolTable parent;
    private final int id;
    private SymbolTable ownScope;
    private final TokenType type;
    private String name; // id/int/reals/strings
    private Token ogToken; // contains line info e.t.c of first declaration
    private Declaration declaration;
    private List<SymbolTableRecord> arguments; // only present if declaration == FUNCTION, also part of ownScope
    private Declaration returnType; // only present if declaration == FUNCTION

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

    public Optional<Declaration> getDeclaration() {
        return Optional.ofNullable(declaration);
    }

    public void setDeclaration(Declaration declaration) {
        this.declaration = declaration;
    }

    public Optional<List<SymbolTableRecord>> getArguments() {
        return Optional.ofNullable(arguments);
    }

    public void addArgument(SymbolTableRecord argument) {
        if (declaration == null || !declaration.equals(Declaration.FUNCTION)) {
            throw new IllegalStateException("Cannot add arguments to a non-function record");
        }
        if (arguments == null) {
            arguments = new LinkedList<>();
        }
        arguments.add(argument);
    }

    public Optional<Declaration> getReturnType() {
        return Optional.ofNullable(returnType);
    }

    public void setReturnType(Declaration returnType) {
        if (declaration == null || !declaration.equals(Declaration.FUNCTION)) {
            throw new IllegalStateException("Cannot set return type to a non-function record");
        }
        this.returnType = returnType;
    }

    public SymbolTable getScope() {
        // TODO: This should be restricted to function identifiers e.t.c.
        if (ownScope == null) {
            ownScope = new SymbolTable(parent);
        }
        return ownScope;
    }
}
