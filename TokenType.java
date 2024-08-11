public enum TokenType {
    // Token value for end of file
    TTEOF(0),
    // The 38 keywords
    TCD24(1), TCONS(2), TTYPD(3), TTDEF(4), TARRD(5), TMAIN(6),
    TBEGN(7), TTEND(8), TARAY(9), TTTOF(10), TFUNC(11), TVOID(12),
    TCNST(13), TINTG(14), TFLOT(15), TBOOL(16), TTFOR(17), TREPT(18),
    TUNTL(19), TTTDO(20), TWHIL(21), TIFTH(22), TELSE(23), TELIF(24),
    TSWTH(25), TCASE(26), TDFLT(27), TBREK(28), TINPT(29), TPRNT(30),
    TPRLN(31), TRETN(32), TNOTT(33), TTAND(34), TTTOR(35), TTXOR(36),
    TTRUE(37), TFALS(38),
    // the operators and delimiters
    TCOMA(39), TLBRK(40), TRBRK(41), TLPAR(42), TRPAR(43), TEQUL(44),
    TPLUS(45), TMINS(46), TSTAR(47), TDIVD(48), TPERC(49), TCART(50),
    TLESS(51), TGRTR(52), TCOLN(53), TSEMI(54), TDOTT(55), TLEQL(56),
    TGEQL(57), TNEQL(58), TEQEQ(59), TPLEQ(60), TMNEQ(61), TSTEQ(62),
    TDVEQ(63),
    // the tokens which need tuple values
    TIDEN(64), TILIT(65), TFLIT(66), TSTRG(67), TUNDF(68);

    private int value;

    TokenType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
