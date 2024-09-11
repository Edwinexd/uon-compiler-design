import java.util.Optional;

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

    // TODO: A proper toString method
    @Override
    public String toString() {
        return nodeType.toString();
    }

}
