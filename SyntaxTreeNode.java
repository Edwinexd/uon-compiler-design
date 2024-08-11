import javax.swing.tree.DefaultMutableTreeNode;

public class SyntaxTreeNode extends DefaultMutableTreeNode  
{
    private Token token;

    public SyntaxTreeNode(Token tokenIn) 
    {
        token = tokenIn;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

}
