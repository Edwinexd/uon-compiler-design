// TODO Changes have been mae in tokens.txt
public enum TokenType {
    // Token value for end of file
    TTEOF(0),
    // The 37 keywords
    TCD24(1),
    TCONS(2),
    TTYPD(3),
    TTDEF(4),
    TARRD(5),
    TMAIN(6),
    TBEGN(7),
    TTEND(8),
    TARAY(9),
    TTTOF(10),
    TFUNC(11),
    TVOID(12),
    TCNST(13),
    TINTG(14),
    TFLOT(15),
    TBOOL(16),
    TTFOR(17),
    TREPT(18),
    TUNTL(19),
    TTTDO(20),
    TWHIL(21),
    TIFTH(22),
    TELSE(23),
    TELIF(24),
    TSWTH(25),
    TCASE(26),
    TDFLT(27),
    TINPT(28),
    TPRNT(29),
    TPRLN(30),
    TRETN(31),
    TNOTT(32),
    TTAND(33),
    TTTOR(34),
    TTXOR(35),
    TTRUE(36),
    TFALS(37),
    // the operators and delimiters
    TCOMA(38),
    TLBRK(39),
    TRBRK(40),
    TLPAR(41),
    TRPAR(42),
    TEQUL(43),
    TPLUS(44),
    TMINS(45),
    TSTAR(46),
    TDIVD(47),
    TPERC(48),
    TCART(49),
    TLESS(50),
    TGRTR(51),
    TCOLN(52),
    TSEMI(53),
    TDOTT(54),
    TLEQL(55),
    TGEQL(56),
    TNEQL(57),
    TEQEQ(58),
    TPLEQ(59),
    TMNEQ(60),
    TSTEQ(61),
    TDVEQ(62),
    // the tokens which need tuple values
    TIDEN(63),
    TILIT(64),
    TFLIT(65),
    TSTRG(66),
    TUNDF(67);

    private int value;

    TokenType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
