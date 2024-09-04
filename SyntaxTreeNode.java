public class SyntaxTreeNode
{
    private Token token;

    private SyntaxTreeNode parent;

    private SyntaxTreeNode firstChild;

    private SyntaxTreeNode secondChild;

    private SyntaxTreeNode thirdChild;

    public SyntaxTreeNode(Token tokenIn) 
    {
        token = tokenIn;
        parent = null;
        firstChild = null;
        secondChild = null;
        thirdChild = null;
    }

    public SyntaxTreeNode(Token tokenIn, SyntaxTreeNode nodeParent, SyntaxTreeNode nodeOne, SyntaxTreeNode nodeTwo, SyntaxTreeNode nodeThree) 
    {
        token = tokenIn;
        parent = nodeParent;
        firstChild = nodeOne;
        secondChild = nodeTwo;
        thirdChild = nodeThree;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setFirstChild(SyntaxTreeNode child)
    {
        firstChild = child;
    }

    public void setSecondChild(SyntaxTreeNode child)
    {
        secondChild = child;
    }

    public void setThirdChild(SyntaxTreeNode child)
    {
        thirdChild = child;
    }

}