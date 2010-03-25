package fitnesse.wikitext.parser;

import java.util.HashMap;

public enum TokenType {
    List(new Matcher().startLine().whitespace().string("*").tokenClass(ListToken.class)),
    Whitespace(new Matcher().whitespace()),
    Newline(new Matcher().string("\n").tokenClass(NewlineToken.class)),
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
    EndSection(new Matcher().startLine().repeat('*').string("!")),
    HorizontalRule(new Matcher().string("---").repeat('-').tokenClass(HorizontalRuleToken.class)),
    Bold(new Matcher().string("'''").tokenClass(EqualPairToken.class)),
    Italic(new Matcher().string("''").tokenClass(EqualPairToken.class)),
    Strike(new Matcher().string("--").tokenClass(EqualPairToken.class)),
    AnchorReference(new Matcher().string(".#").tokenClass(AnchorReferenceToken.class)),

    Table(new Matcher().startLine().string(new String[] {"|", "!|"}).tokenClass(TableToken.class)),
    EndCell(new Matcher().string(new String[] {"|\n|", "|\n", "|"})),
    
    HashTable(new Matcher().string("!{").tokenClass(HashTableToken.class)),
    HeaderLine(new Matcher().startLine().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}).tokenClass(LineToken.class)),
    OpenLiteral(new Matcher().string("!-").tokenClass(OpenLiteralToken.class)),
    Collapsible(new Matcher().startLine().string("!").repeat('*').tokenClass(CollapsibleToken.class)),
    AnchorName(new Matcher().string("!anchor").tokenClass(AnchorNameToken.class)),
    CenterLine(new Matcher().startLine().string(new String[] {"!c", "!C"}).tokenClass(LineToken.class)),
    Contents(new Matcher().startLine().string("!contents").tokenClass(ContentsToken.class)),
    Define(new Matcher().startLine().string("!define").tokenClass(DefineToken.class)),
    Include(new Matcher().startLine().string("!include").tokenClass(IncludeToken.class)),
    NoteLine(new Matcher().startLine().string("!note").tokenClass(LineToken.class)),
    Style(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}).tokenClass(StyleToken.class)),

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

    /* Keeping these tables in sync with the matchers is a hassle but a major performance gain */

    private static HashMap<Character, TokenType[]> dispatch;
    static {
        dispatch = new HashMap<Character, TokenType[]>();
        dispatch.put('|', new TokenType[]
            { Table, EndCell });
        dispatch.put('!', new TokenType[]
            { HashTable, HeaderLine, OpenLiteral, Collapsible, AnchorName, Contents, CenterLine, Define, Include, NoteLine, Style, Table });
        for (char letter = 'a'; letter <= 'z'; letter++) dispatch.put(letter, new TokenType[] {});
        for (char letter = 'A'; letter <= 'Z'; letter++) dispatch.put(letter, new TokenType[] {});
        for (char digit = '0'; digit <= '9'; digit++) dispatch.put(digit, new TokenType[] {});
    }

    /* These can be broken out further */
    private static final TokenType[] otherTokens = new TokenType[]
            { List, Whitespace, Newline, Colon, Comma, Evaluator, CloseEvaluator, Variable, Preformat, ClosePreformat, OpenParenthesis,
              OpenBrace, OpenBracket, CloseParenthesis, CloseBrace, CloseBracket, CloseLiteral, Collapsible, EndSection, HorizontalRule,
              Bold, Italic, Strike, AnchorReference };

    public static TokenType[] getMatchTypes(Character match) {
        if (dispatch.containsKey(match)) return dispatch.get(match);
        return otherTokens;
    }

    private Matcher matcher;

    TokenType(Matcher matcher) { this.matcher = matcher; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }
}

