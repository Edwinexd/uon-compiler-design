public class CharacterTypes {
    public static boolean isDelimiter(char c) {
        return 
            c == ',' || 
            c == ';' || 
            c == '[' || 
            c == ']' || 
            c == '(' || 
            c == ')' || 
            c == '=' || 
            c == '+' || 
            c == '-' || 
            c == '*' || 
            c == '/' || 
            c == '%' || 
            c == '^' || 
            c == '<' || 
            c == '>' || 
            c == ':' || 
            c == '.' || 
            c == ' ' || 
            c == '\n';
    }

    public static boolean isWhitespace(char c) {
        return 
        c == ' ' || 
        c == '\n';
    }
}
