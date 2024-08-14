import javax.swing.JTree;

public class Parser 
{
    // im assuming we will be creating a tree. from the lecture notes it looks like the tree
    // im guessing that the structure will be somthing like

    // token =>  


    // built after
    private JTree syntaxTree = new JTree();

    private SyntaxTreeNode root = new SyntaxTreeNode(null);

    // sort of a pointer but not really
    // will act as a pointer when setting children but then when i set it to one of the children
    // it should not reser the current one
    private SyntaxTreeNode current;

    public void pass(Token token) 
    {
        // level 1 of the tree

        // CD24
        if (token.getType() == TokenType.TIDEN)
        {
            addToRoot(token);
        }
        // begin
        else if (token.getType() == TokenType.TBEGN)
        {
            addToRoot(token);
        }
        // main
        else if (token.getType() == TokenType.TMAIN)
        {
            addToRoot(token);
        }

        // So what i am thinking with this is i need to have the base methods in the first layer of the tree
        // then after the first layer of the tree i will have the current and if it is not any of the
        // other identifiers it will add to the first layer. But if a new token comes through then i will be
        // at the current and it will skip over all of the ones above and i will need to implement the logic
        // for that below...

        // then ill have to look more at the structure and part the red seas


    }


    private void addToRoot(Token token)
    {
        // add to root
        SyntaxTreeNode node = new SyntaxTreeNode(token);

        root.add(node);

        // maybe??
        current = node;
    }





}
