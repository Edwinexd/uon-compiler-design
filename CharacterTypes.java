/**
 * Methods for classifying characters.
 * 
 * @author Edwin Sundberg
 * @author Benjamin Napoli
 */
public class CharacterTypes {
    public static boolean isDelimiter(char c) {
        return c == ',' ||
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
                c == '\t' ||
                c == '\n';
    }

    public static boolean isWhitespace(char c) {
        return c == ' ' ||
                c == '\t' ||
                c == '\n';
    }
}
