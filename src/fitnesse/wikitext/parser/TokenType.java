package fitnesse.wikitext.parser;

public enum TokenType {
    List(new Matcher().startLine().whitespace().string("*")),
    Whitespace(new Matcher().whitespace()),
    Table(new Matcher().startLine().string(new String[] {"|", "!|"}).tokenClass(TableToken.class)),
    Colon(new Matcher().string(":")),
    Comma(new Matcher().string(",")),
    Evaluator(new Matcher().string("${=").tokenClass(EvaluatorToken.class)),
    CloseEvaluator(new Matcher().string("=}")),
    Variable(new Matcher().string("${").tokenClass(VariableToken.class)),
    HashTable(new Matcher().string("!{").tokenClass(HashTableToken.class)),
    Preformat(new Matcher().string("{{{").tokenClass(PreformatToken.class)),
    ClosePreformat(new Matcher().string("}}}")),
    OpenParenthesis(new Matcher().string("(")),
    OpenBrace(new Matcher().string("{")),
    OpenBracket(new Matcher().string("[")),
    CloseParenthesis(new Matcher().string(")")),
    CloseBrace(new Matcher().string("}")),
    CloseBracket(new Matcher().string("]")),
    Newline(new Matcher().string("\n").tokenClass(NewlineToken.class)),
    OpenLiteral(new Matcher().string("!-").tokenClass(OpenLiteralToken.class)),
    CloseLiteral(new Matcher().string("-!")),
    Collapsible(new Matcher().startLine().string("!").repeat('*').tokenClass(CollapsibleToken.class)),
    EndSection(new Matcher().startLine().repeat('*').string("!")),
    HorizontalRule(new Matcher().string("---").repeat('-').tokenClass(HorizontalRuleToken.class)),
    Bold(new Matcher().string("'''").tokenClass(EqualPairToken.class)),
    Italic(new Matcher().string("''").tokenClass(EqualPairToken.class)),
    Strike(new Matcher().string("--").tokenClass(EqualPairToken.class)),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}).tokenClass(StyleToken.class)),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    AnchorReference(new Matcher().string(".#").tokenClass(AnchorReferenceToken.class)),
    HeaderLine(new Matcher().startLine().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}).tokenClass(LineToken.class)),
    AnchorName(new Matcher().string("!anchor").tokenClass(AnchorNameToken.class)),
    Contents(new Matcher().startLine().string("!contents").tokenClass(ContentsToken.class)),
    CenterLine(new Matcher().startLine().string(new String[] {"!c", "!C"}).tokenClass(LineToken.class)),
    Define(new Matcher().startLine().string("!define").tokenClass(DefineToken.class)),
    Include(new Matcher().startLine().string("!include").tokenClass(IncludeToken.class)),
    NoteLine(new Matcher().startLine().string("!note").tokenClass(LineToken.class)),
    Text(new Matcher().noMatch()),
    Empty(new Matcher().noMatch());

    public static TokenType closeType(TokenType type) {
        return type == OpenBrace ? CloseBrace
                : type == OpenBracket ? CloseBracket
                : type == OpenParenthesis ? CloseParenthesis
                : Empty;
    }

    public static  TokenType closeType(char beginner) {
        return beginner == '[' ? TokenType.CloseBracket
                : beginner == '{' ? TokenType.CloseBrace
                : TokenType.CloseParenthesis;
    }

    private Matcher matcher;

    TokenType(Matcher matcher) { this.matcher = matcher; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }
}

