package fitnesse.wikitext.parser;

public enum SymbolType implements Matchable {
    Alias(new Matcher().string("[[")),
    AnchorName(new Matcher().string("!anchor")),
    AnchorReference(new Matcher().string(".#")),
    Bold(new Matcher().string("'''")),
    CenterLine(new Matcher().string(new String[] {"!c", "!C"})),
    CloseBrace(new Matcher().string("}")),
    CloseBracket(new Matcher().string("]")),
    CloseCollapsible(new Matcher().string("\n").repeat('*').string("!")),
    CloseEvaluator(new Matcher().string("=}")),
    CloseLiteral(new Matcher().string("-!")),
    CloseParenthesis(new Matcher().string(")")),
    ClosePlainTextTable(new Matcher().startLine().string("]!")),
    ClosePreformat(new Matcher().string("}}}")),
    Collapsible(new Matcher().startLine().string("!").repeat('*')),
    Colon(new Matcher().string(":")),
    Comma(new Matcher().string(",")),
    Comment(new Matcher().startLine().string("#")),
    Contents(new Matcher().string("!contents")),
    Define(new Matcher().startLine().string("!define")),
    EMail(),
    Empty(),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    Evaluator(new Matcher().string("${=")),
    HashTable(new Matcher().string("!{")),
    HeaderLine(new Matcher().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"})),
    HorizontalRule(new Matcher().string("---").repeat('-')),
    Image(new Matcher().string(new String[] {"!img-l", "!img-r", "!img"})),
    Include(new Matcher().startLine().string("!include")),
    Italic(new Matcher().string("''")),
    LastModified(new Matcher().string("!lastmodified")),
    Link(new Matcher().string(new String[] {"http://", "https://"})),
    Literal(new Matcher().string("!-")),
    Meta(new Matcher().string("!meta")),
    Newline(new Matcher().string("\n")),
    NoteLine(new Matcher().string("!note")),
    OpenBrace(new Matcher().string("{")),
    OpenBracket(new Matcher().string("[")),
    OpenParenthesis(new Matcher().string("(")),
    OrderedList(new Matcher().startLine().whitespace().string(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"}).string(" ")),
    Path(new Matcher().startLine().string("!path")),
    PlainTextCellSeparator(),
    PlainTextTable(new Matcher().startLine().string("![")),
    Preformat(new Matcher().string("{{{")),
    See(new Matcher().string("!see").whitespace()),
    Strike(new Matcher().string("--")),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['})),
    SymbolList(),
    Table(new Matcher().startLine().string(new String[] {"|", "!|", "-|", "-!|"})),
    Today(new Matcher().string("!today")),
    Text(),
    UnorderedList(new Matcher().startLine().whitespace().string("* ")),
    Variable(new Matcher().string("${")),
    Whitespace(new Matcher().whitespace()),
    WikiWord();

    public SymbolType closeType() {
        return this == OpenBrace ? CloseBrace
                : this == OpenBracket ? CloseBracket
                : this == OpenParenthesis ? CloseParenthesis
                : this == Literal ? CloseLiteral
                : this == Preformat ? ClosePreformat
                : this == Comment ? Newline
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

