import javax.swing.JTree;

public class Parser 
{
    // im assuming we will be creating a tree. from the lecture notes it looks like the tree
    // im guessing that the structure will be somthing like

    // token =>  



    private JTree syntaxTree = new JTree();

    public void pass(Token token) 
    {
        if (token.getType() == TokenType.TIDEN)
        {
            SyntaxTreeNode node = new SyntaxTreeNode(token);

            //syntaxTree.add(node);
        }



    }
}
