package fitnesse.wikitext.parser;

public enum SymbolType {
    Alias(new Matcher().string("[["), new AliasRule()),
    AnchorName(new Matcher().string("!anchor"), new AnchorNameRule()),
    AnchorReference(new Matcher().string(".#"), new AnchorReferenceRule()),
    Bold(new Matcher().string("'''"), new EqualPairRule()),
    CenterLine(new Matcher().string(new String[] {"!c", "!C"}), new LineRule()),
    CloseBrace(new Matcher().string("}")),
    CloseBracket(new Matcher().string("]")),
    CloseCollapsible(new Matcher().startLine().repeat('*').string("!")),
    CloseEvaluator(new Matcher().string("=}")),
    CloseLiteral(new Matcher().string("-!")),
    CloseParenthesis(new Matcher().string(")")),
    ClosePlainTextTable(new Matcher().startLine().string("]!")),
    ClosePreformat(new Matcher().string("}}}")),
    Collapsible(new Matcher().startLine().string("!").repeat('*'), new CollapsibleRule()),
    Colon(new Matcher().string(":")),
    Comma(new Matcher().string(",")),
    Comment(new Matcher().startLine().string("#"), new CommentRule()),
    Contents(new Matcher().string("!contents"), new ContentsRule()),
    Define(new Matcher().startLine().string("!define"), new DefineRule()),
    EMail(),
    Empty(),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    Evaluator(new Matcher().string("${="), new EvaluatorRule()),
    HashTable(new Matcher().string("!{"), new HashTableRule()),
    HeaderLine(new Matcher().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}), new LineRule()),
    HorizontalRule(new Matcher().string("---").repeat('-')),
    Include(new Matcher().startLine().string("!include"), new IncludeRule()),
    Italic(new Matcher().string("''"), new EqualPairRule()),
    Link(new Matcher().string(new String[] {"http://", "https://"}), new LinkRule()),
    Literal(new Matcher().string("!-"), new LiteralRule()),
    Meta(new Matcher().string("!meta"), new LineRule()),
    Newline(new Matcher().string("\n")),
    NoteLine(new Matcher().string("!note"), new LineRule()),
    OpenBrace(new Matcher().string("{")),
    OpenBracket(new Matcher().string("[")),
    OpenParenthesis(new Matcher().string("(")),
    OrderedList(new Matcher().startLine().whitespace().string(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"}).string(" "), new ListRule()),
    Path(new Matcher().startLine().string("!path"), new LineRule()),
    PlainTextTable(new Matcher().startLine().string("!["), new PlainTextTableRule()),
    Preformat(new Matcher().string("{{{"), new LiteralRule()),
    See(new Matcher().string("!see").whitespace(), new SeeRule()),
    Strike(new Matcher().string("--"), new EqualPairRule()),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}), new StyleRule()),
    SymbolList(),
    Table(new Matcher().startLine().string(new String[] {"|", "!|", "-|", "-!|"}), new TableRule()),
    Text(),
    UnorderedList(new Matcher().startLine().whitespace().string("* "), new ListRule()),
    Variable(new Matcher().string("${"), new VariableRule()),
    Whitespace(new Matcher().whitespace()),
    WikiWord();

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

