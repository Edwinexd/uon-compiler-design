import java.util.Optional;

/**
 * Represents a node in the syntax tree.
 * 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class SyntaxTreeNode {
    private TreeNodeType nodeType;

    private Token nodeValue;
    private SymbolTableRecord valueRecord;

    private SyntaxTreeNode firstChild;

    private SyntaxTreeNode secondChild;

    private SyntaxTreeNode thirdChild;

    public SyntaxTreeNode(TreeNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public SyntaxTreeNode(TreeNodeType nodeType, Token nodeValue, SymbolTableRecord valueRecord) {
        this.nodeType = nodeType;
        this.nodeValue = nodeValue;
        this.valueRecord = valueRecord;
    }

    public TreeNodeType getNodeType() {
        return nodeType;
    }

    public Optional<Token> getNodeValue() {
        return Optional.ofNullable(nodeValue);
    }

    public void setNodeValue(Token token) {
        this.nodeValue = token;
    }

    public Optional<SymbolTableRecord> getValueRecord() {
        return Optional.ofNullable(valueRecord);
    }

    public void setValueRecord(SymbolTableRecord record) {
        this.valueRecord = record;
    }

    public void setFirstChild(SyntaxTreeNode child) {
        firstChild = child;
    }

    public void setSecondChild(SyntaxTreeNode child) {
        secondChild = child;
    }

    public void setThirdChild(SyntaxTreeNode child) {
        thirdChild = child;
    }

    public Optional<SyntaxTreeNode> getFirstChild() {
        return Optional.ofNullable(firstChild);
    }

    public Optional<SyntaxTreeNode> getSecondChild() {
        return Optional.ofNullable(secondChild);
    }

    public Optional<SyntaxTreeNode> getThirdChild() {
        return Optional.ofNullable(thirdChild);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(nodeType.toString());
        if (nodeValue != null) {
            output.append(" ");
            output.append(nodeValue.getLexeme());
        }
        output.append(" ");
        int remainder = output.length() % 7;
        if (remainder != 0) {
            for (int i = 0; i < 7 - remainder; i++) {
                output.append(" ");
            }
        }
        return output.toString();
    }
}
