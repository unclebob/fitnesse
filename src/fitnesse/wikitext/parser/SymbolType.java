package fitnesse.wikitext.parser;

public enum SymbolType {
    List(new Matcher().startLine().whitespace().string("*").ruleClass(ListRule.class)),
    Comment(new Matcher().startLine().string("#").ruleClass(CommentRule.class)),
    Whitespace(new Matcher().whitespace()),
    Newline(new Matcher().string("\n")),
    Colon(new Matcher().string(":")),
    Comma(new Matcher().string(",")),
    Evaluator(new Matcher().string("${=").ruleClass(EvaluatorRule.class)),
    CloseEvaluator(new Matcher().string("=}")),
    Variable(new Matcher().string("${").ruleClass(VariableRule.class)),
    Preformat(new Matcher().string("{{{").ruleClass(LiteralRule.class)),
    ClosePreformat(new Matcher().string("}}}")),
    OpenParenthesis(new Matcher().string("(")),
    OpenBrace(new Matcher().string("{")),
    OpenBracket(new Matcher().string("[")),
    CloseParenthesis(new Matcher().string(")")),
    CloseBrace(new Matcher().string("}")),
    CloseBracket(new Matcher().string("]")),
    CloseLiteral(new Matcher().string("-!")),
    CloseCollapsible(new Matcher().startLine().repeat('*').string("!")),
    HorizontalRule(new Matcher().string("---").repeat('-')),
    Bold(new Matcher().string("'''").ruleClass(EqualPairRule.class)),
    Italic(new Matcher().string("''").ruleClass(EqualPairRule.class)),
    Strike(new Matcher().string("--").ruleClass(EqualPairRule.class)),
    AnchorReference(new Matcher().string(".#").ruleClass(AnchorReferenceRule.class)),

    Table(new Matcher().startLine().string(new String[] {"|", "!|"}).ruleClass(TableRule.class)),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    
    HashTable(new Matcher().string("!{").ruleClass(HashTableRule.class)),
    HeaderLine(new Matcher().startLine().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}).ruleClass(LineRule.class)),
    Literal(new Matcher().string("!-").ruleClass(LiteralRule.class)),
    Collapsible(new Matcher().startLine().string("!").repeat('*').ruleClass(CollapsibleRule.class)),
    AnchorName(new Matcher().string("!anchor").ruleClass(AnchorNameRule.class)),
    CenterLine(new Matcher().startLine().string(new String[] {"!c", "!C"}).ruleClass(LineRule.class)),
    Contents(new Matcher().startLine().string("!contents").ruleClass(ContentsRule.class)),
    Define(new Matcher().startLine().string("!define").ruleClass(DefineRule.class)),
    Include(new Matcher().startLine().string("!include").ruleClass(IncludeRule.class)),
    NoteLine(new Matcher().startLine().string("!note").ruleClass(LineRule.class)),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}).ruleClass(StyleRule.class)),

    WikiWord(),
    Text(),
    Empty(),
    SymbolList();

    public static SymbolType closeType(SymbolType type) {
        return type == OpenBrace ? CloseBrace
                : type == OpenBracket ? CloseBracket
                : type == OpenParenthesis ? CloseParenthesis
                : type == Literal ? CloseLiteral
                : type == Preformat ? ClosePreformat
                : type == Comment ? Newline
                : Empty;
    }

    public static SymbolType closeType(char beginner) {
        return beginner == '[' ? SymbolType.CloseBracket
                : beginner == '{' ? SymbolType.CloseBrace
                : SymbolType.CloseParenthesis;
    }

    private Matcher matcher;

    SymbolType() { this.matcher = new Matcher().noMatch(); }
    SymbolType(Matcher matcher) { this.matcher = matcher; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }
}

