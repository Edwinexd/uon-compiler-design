import javax.swing.JTree;

public class Parser 
{
    // im assuming we will be creating a tree. from the lecture notes it looks like the tree
    // im guessing that the structure will be somthing like

    // token =>  


    // built after
    private JTree syntaxTree = new JTree();

    private SyntaxTreeNode root = new SyntaxTreeNode(null);

    public void pass(Token token) 
    {
        if (token.getType() == TokenType.TIDEN)
        {
            // add to root

            SyntaxTreeNode node = new SyntaxTreeNode(token);

            root.add(node);

            //syntaxTree.add(node);
        }




    }




}
