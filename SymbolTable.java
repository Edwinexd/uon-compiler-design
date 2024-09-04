import java.util.HashMap;
import java.util.Optional;

/** 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 * "Recursively" defined symbol table, e.x. a function may have its own symbol table within a SymbolTableRecord
 * and subsequent lookups in the inner symbol table will look up the parent symbol table if the entry is not found.
 * Technically overkill since we can at most have 2 levels in CD24.
*/
public class SymbolTable {
    private final SymbolTable parent;
    // string is the id/int/reals/strings
    private final HashMap<String, SymbolTableRecord> map = new HashMap<>();
    private int nextId = 0;
    
    /**
     * Root of symbol table
    */
    public SymbolTable() {
        this(null);
    }

    protected SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    protected int getNextId() {
        if (parent != null) {
            return parent.getNextId();
        }
        return nextId++;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public SymbolTableRecord getOrCreateToken(String name, Token token) {
        Optional<SymbolTableRecord> existingRecord = getToken(name);
        if (existingRecord.isPresent()) {
            return existingRecord.get();
        }

        SymbolTableRecord record = new SymbolTableRecord(this, token.getType(), name, token);
        map.put(name, record);
        return record;
    }

    public Optional<SymbolTableRecord> getToken(String name) {
        if (map.containsKey(name)) {
            return Optional.of(map.get(name));
        }

        if (parent != null) {
            return parent.getToken(name);
        }

        // Root of tree and no entry found
        return Optional.empty();
    }

    // TODO: Not sure if we actually need this
    public boolean delete(String name) {
        if (map.containsKey(name)) {
            map.remove(name);
            return true;
        }

        if (parent != null) {
            return parent.delete(name);
        }

        return false;
    }

}
