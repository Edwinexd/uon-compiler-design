import java.util.HashMap;
import java.util.Optional;

// Solution for having enums in map on initialization,
// inspired via Stackoverflow answer by Ori Marko on: https://stackoverflow.com/questions/65301070/making-an-enum-map-from-the-enum-constructor-for-a-static-map
class Mappings {
    protected static final HashMap<Character, TokenType> singleCharDelimiterMap = new HashMap<>();
    protected static final HashMap<Character, HashMap<Character, TokenType>> twoCharDelimiterMap = new HashMap<>();
    protected static final HashMap<String, TokenType> keywordMap = new HashMap<>();
}

public enum TokenType {
    // Token value for end of file
    TTEOF(0),
    // The 38 keywords
    TCD24(1, "cd24"),
    TCONS(2, "constants"),
    TTYPD(3, "typedef"),
    TTDEF(4, "def"),
    TARRD(5, "arraydef"),
    TMAIN(6, "main"),
    TBEGN(7, "begin"),
    TTEND(8, "end"),
    TARAY(9, "array"),
    TTTOF(10, "of"),
    TFUNC(11, "func"),
    TVOID(12, "void"),
    TCNST(13, "const"),
    TINTG(14, "int"),
    TFLOT(15, "float"),
    TBOOL(16, "bool"),
    TTFOR(17, "for"),
    TREPT(18, "repeat"),
    TUNTL(19, "until"),
    TTTDO(20, "do"),
    TWHIL(21, "while"),
    TIFTH(22, "if"),
    TELSE(23, "else"),
    TELIF(24, "elif"),
    TSWTH(25, "switch"),
    TCASE(26, "case"),
    TDFLT(27, "default"),
    TBREK(28, "break"),
    TINPT(29, "input"),
    TPRNT(30, "print"),
    TPRLN(31, "printline"),
    TRETN(32, "return"),
    TNOTT(33, "not"),
    TTAND(34, "and"),
    TTTOR(35, "or"),
    TTXOR(36, "xor"),
    TTRUE(37, "true"),
    TFALS(38, "false"),
    // the operators and delimiters
    TCOMA(39, ','),
    TLBRK(40, '['),
    TRBRK(41, ']'),
    TLPAR(42, '('),
    TRPAR(43, ')'),
    TEQUL(44, '='),
    TPLUS(45, '+'),
    TMINS(46, '-'),
    TSTAR(47, '*'),
    TDIVD(48, '/'),
    TPERC(49, '%'),
    TCART(50, '^'),
    TLESS(51, '<'),
    TGRTR(52, '>'),
    TCOLN(53, ':'),
    TSEMI(54, ';'),
    TDOTT(55, '.'),
    TLEQL(56, '<', '='),
    TGEQL(57, '>', '='),
    TNEQL(58, '!', '='),
    TEQEQ(59, '=', '='),
    TPLEQ(60, '+', '='),
    TMNEQ(61, '-', '='),
    TSTEQ(62, '*', '='),
    TDVEQ(63, '/', '='),
    // the tokens which need tuple values
    TIDEN(64),
    TILIT(65),
    TFLIT(66),
    TSTRG(67),
    TUNDF(68);

    private final int value;

    TokenType(int value) {
        this.value = value;
    }

    TokenType(int value, String keyword) {
        this(value);
        Mappings.keywordMap.put(keyword.toLowerCase(), this);
    }

    TokenType(int value, char... delimiterChars) {
        this(value);
        if (delimiterChars.length > 2) {
            throw new IllegalArgumentException("DelimiterChars must be of length 2 or less");
        }
        if (delimiterChars.length == 2) {
            Mappings.twoCharDelimiterMap.put(delimiterChars[0], new HashMap<>());
            Mappings.twoCharDelimiterMap.get(delimiterChars[0]).put(delimiterChars[1], this);
        } else {
            Mappings.singleCharDelimiterMap.put(delimiterChars[0], this);
        }
    }

    public int getValue() {
        return value;
    }

    public static Optional<TokenType> fromDelimiter(char... delimiterChars) {
        if (delimiterChars.length > 2) {
            throw new IllegalArgumentException("DelimiterChars must be of length 2 or less");
        }
        if (delimiterChars.length == 2) {
            HashMap<Character, TokenType> map = Mappings.twoCharDelimiterMap.get(delimiterChars[0]);
            if (map == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(map.get(delimiterChars[1]));
        } else {
            return Optional.ofNullable(Mappings.singleCharDelimiterMap.get(delimiterChars[0]));
        }
    }

    public static TokenType fromIdentifier(String keyword) {
        TokenType t = Mappings.keywordMap.get(keyword.toLowerCase());
        if (t == null) {
            return TIDEN;
        }
        return t;
    }

}
