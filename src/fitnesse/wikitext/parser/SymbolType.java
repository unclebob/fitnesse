package fitnesse.wikitext.parser;

import java.util.HashMap;

public enum SymbolType {
    List(new Matcher().startLine().whitespace().string("*").tokenClass(ListToken.class)),
    Whitespace(new Matcher().whitespace()),
    Newline(new Matcher().string("\n")),
    Colon(new Matcher().string(":")),
    Comma(new Matcher().string(",")),
    Evaluator(new Matcher().string("${=").tokenClass(EvaluatorToken.class)),
    CloseEvaluator(new Matcher().string("=}")),
    Variable(new Matcher().string("${").tokenClass(VariableToken.class)),
    Preformat(new Matcher().string("{{{").tokenClass(PreformatToken.class)),
    ClosePreformat(new Matcher().string("}}}")),
    OpenParenthesis(new Matcher().string("(")),
    OpenBrace(new Matcher().string("{")),
    OpenBracket(new Matcher().string("[")),
    CloseParenthesis(new Matcher().string(")")),
    CloseBrace(new Matcher().string("}")),
    CloseBracket(new Matcher().string("]")),
    CloseLiteral(new Matcher().string("-!")),
    CloseCollapsible(new Matcher().startLine().repeat('*').string("!")),
    HorizontalRule(new Matcher().string("---").repeat('-').tokenClass(HorizontalRuleToken.class)),
    Bold(new Matcher().string("'''").ruleClass(EqualPairRule.class)),
    Italic(new Matcher().string("''").ruleClass(EqualPairRule.class)),
    Strike(new Matcher().string("--").ruleClass(EqualPairRule.class)),
    AnchorReference(new Matcher().string(".#").ruleClass(AnchorReferenceRule.class)),

    Table(new Matcher().startLine().string(new String[] {"|", "!|"}).tokenClass(TableToken.class)),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    
    HashTable(new Matcher().string("!{").tokenClass(HashTableToken.class)),
    HeaderLine(new Matcher().startLine().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}).ruleClass(LineRule.class)),
    OpenLiteral(new Matcher().string("!-").tokenClass(OpenLiteralToken.class)),
    Collapsible(new Matcher().startLine().string("!").repeat('*').ruleClass(CollapsibleRule.class)),
    AnchorName(new Matcher().string("!anchor").ruleClass(AnchorNameRule.class)),
    CenterLine(new Matcher().startLine().string(new String[] {"!c", "!C"}).ruleClass(LineRule.class)),
    Contents(new Matcher().startLine().string("!contents").ruleClass(ContentsRule.class)),
    Define(new Matcher().startLine().string("!define").ruleClass(DefineRule.class)),
    Include(new Matcher().startLine().string("!include").tokenClass(IncludeToken.class)),
    NoteLine(new Matcher().startLine().string("!note").ruleClass(LineRule.class)),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}).tokenClass(StyleToken.class)),

    //WikiWord(new Matcher().noMatch()),
    Text(),
    Empty(),

    SymbolList();

    public static SymbolType closeType(SymbolType type) {
        return type == OpenBrace ? CloseBrace
                : type == OpenBracket ? CloseBracket
                : type == OpenParenthesis ? CloseParenthesis
                : Empty;
    }

    public static SymbolType closeType(char beginner) {
        return beginner == '[' ? SymbolType.CloseBracket
                : beginner == '{' ? SymbolType.CloseBrace
                : SymbolType.CloseParenthesis;
    }

    /* Keeping these tables in sync with the matchers is a hassle but a major performance gain */

    private static HashMap<Character, SymbolType[]> dispatch;
    static {
        dispatch = new HashMap<Character, SymbolType[]>();
        dispatch.put('|', new SymbolType[]
            { Table, EndCell });
        dispatch.put('!', new SymbolType[]
            { HashTable, HeaderLine, OpenLiteral, Collapsible, AnchorName, Contents, CenterLine, Define, Include, NoteLine, Style, Table });
        for (char letter = 'a'; letter <= 'z'; letter++) dispatch.put(letter, new SymbolType[] {});
        for (char letter = 'A'; letter <= 'Z'; letter++) dispatch.put(letter, new SymbolType[] {});
        for (char digit = '0'; digit <= '9'; digit++) dispatch.put(digit, new SymbolType[] {});
    }

    /* These can be broken out further */
    private static final SymbolType[] otherTokens = new SymbolType[]
            { List, Whitespace, Newline, Colon, Comma, Evaluator, CloseEvaluator, Variable, Preformat, ClosePreformat, OpenParenthesis,
              OpenBrace, OpenBracket, CloseParenthesis, CloseBrace, CloseBracket, CloseLiteral, Collapsible, CloseCollapsible, HorizontalRule,
              Bold, Italic, Strike, AnchorReference};

    public static SymbolType[] getMatchTypes(Character match) {
        if (dispatch.containsKey(match)) return dispatch.get(match);
        return otherTokens;
    }

    private Matcher matcher;

    SymbolType() { this.matcher = new Matcher().noMatch(); }
    SymbolType(Matcher matcher) { this.matcher = matcher; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }
}

