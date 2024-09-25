import java.util.Optional;

/**
 * Represents a declaration in the symbol table. Everything from an integer to a "instance" of a struct.
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class Declaration {
    public static final Declaration PROGRAM = new Declaration(DeclarationType.PROGRAM);
    public static final Declaration INT = new Declaration(DeclarationType.INT);
    public static final Declaration FLOAT = new Declaration(DeclarationType.FLOAT);
    public static final Declaration BOOL = new Declaration(DeclarationType.BOOL);
    public static final Declaration VOID = new Declaration(DeclarationType.VOID);
    public static final Declaration STRUCT_TYPE = new Declaration(DeclarationType.STRUCT_TYPE);
    public static final Declaration ARRAY_TYPE = new Declaration(DeclarationType.ARRAY_TYPE);
    public static final Declaration FUNCTION = new Declaration(DeclarationType.FUNCTION);
    public static final Declaration CONSTANT = new Declaration(DeclarationType.CONSTANT);

    private final DeclarationType type;
    private SymbolTableRecord record;

    private Declaration(DeclarationType type) {
        this.type = type;
    }

    public static Declaration structOfType(SymbolTableRecord record) {
        Declaration declaration = new Declaration(DeclarationType.STRUCT);
        declaration.record = record;
        return declaration;
    }

    public static Declaration arrayOfType(SymbolTableRecord record) {
        Declaration declaration = new Declaration(DeclarationType.ARRAY);
        declaration.record = record;
        return declaration;
    }

    public static Declaration arrayConstantOfType(SymbolTableRecord record) {
        Declaration declaration = new Declaration(DeclarationType.ARRAY_CONSTANT);
        declaration.record = record;
        return declaration;
    }

    public DeclarationType getType() {
        return type;
    }

    public Optional<SymbolTableRecord> getRecord() {
        return Optional.ofNullable(record);
    }

    public boolean isPrimitive() {
        return type == DeclarationType.INT || type == DeclarationType.FLOAT || type == DeclarationType.BOOL || type == DeclarationType.VOID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Declaration) {
            Declaration other = (Declaration) obj;
            if (type != other.type) {
                return false;
            }
            if (!type.requiresSymbolTableRecord()) {
                return true;
            }
            return record.equals(other.record);
        }
        return false;
    }
}
