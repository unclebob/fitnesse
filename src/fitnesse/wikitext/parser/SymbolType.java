package fitnesse.wikitext.parser;

public enum SymbolType {
    Alias(new Matcher().string("[["), new AliasRule()),
    List(new Matcher().startLine().whitespace().string("*"), new ListRule()),
    Comment(new Matcher().startLine().string("#"), new CommentRule()),
    Whitespace(new Matcher().whitespace()),
    Newline(new Matcher().string("\n")),
    Colon(new Matcher().string(":")),
    Comma(new Matcher().string(",")),
    Evaluator(new Matcher().string("${="), new EvaluatorRule()),
    CloseEvaluator(new Matcher().string("=}")),
    Variable(new Matcher().string("${"), new VariableRule()),
    Preformat(new Matcher().string("{{{"), new LiteralRule()),
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
    Bold(new Matcher().string("'''"), new EqualPairRule()),
    Italic(new Matcher().string("''"), new EqualPairRule()),
    Strike(new Matcher().string("--"), new EqualPairRule()),
    AnchorReference(new Matcher().string(".#"), new AnchorReferenceRule()),

    Table(new Matcher().startLine().string(new String[] {"|", "!|"}), new TableRule()),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    
    HashTable(new Matcher().string("!{"), new HashTableRule()),
    HeaderLine(new Matcher().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}), new LineRule()),
    Literal(new Matcher().string("!-"), new LiteralRule()),
    Collapsible(new Matcher().startLine().string("!").repeat('*'), new CollapsibleRule()),
    AnchorName(new Matcher().string("!anchor"), new AnchorNameRule()),
    CenterLine(new Matcher().string(new String[] {"!c", "!C"}), new LineRule()),
    Contents(new Matcher().string("!contents"), new ContentsRule()),
    Define(new Matcher().startLine().string("!define"), new DefineRule()),
    Include(new Matcher().startLine().string("!include"), new IncludeRule()),
    Meta(new Matcher().string("!meta"), new LineRule()),
    NoteLine(new Matcher().string("!note"), new LineRule()),
    Path(new Matcher().startLine().string("!path"), new LineRule()),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}), new StyleRule()),

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
    private Rule rule;

    SymbolType() { this.matcher = new Matcher().noMatch(); }
    SymbolType(Matcher matcher) { this.matcher = matcher; }
    SymbolType(Matcher matcher, Rule rule) {
        this.matcher = matcher;
        this.rule = rule;
    }

    public Rule getRule() { return rule; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }
}

