package fitnesse.wikitext.parser;

public class SymbolType {
    
    public static final SymbolType Bold = new SymbolType("Bold")
            .wikiMatcher(new Matcher().string("'''"))
            .wikiRule(new EqualPairRule())
            .htmlTranslation(new HtmlBuilder("b").body(0).inline());
    public static final SymbolType CenterLine = new SymbolType("CenterLine")
            .wikiMatcher(new Matcher().string(new String[] {"!c", "!C"}))
            .wikiRule(new LineRule())
            .htmlTranslation(new HtmlBuilder("div").body(0).attribute("class", "centered"));
    public static final SymbolType CloseBrace = new SymbolType("CloseBrace")
            .wikiMatcher(new Matcher().string("}"));
    public static final SymbolType CloseBracket = new SymbolType("CloseBracket")
            .wikiMatcher(new Matcher().string("]"));
    public static final SymbolType CloseCollapsible = new SymbolType("CloseCollapsible")
            .wikiMatcher(new Matcher().string("\n").repeat('*').string("!"));
    public static final SymbolType CloseEvaluator = new SymbolType("CloseEvaluator")
            .wikiMatcher(new Matcher().string("=}"));
    public static final SymbolType CloseLiteral = new SymbolType("CloseLiteral")
            .wikiMatcher(new Matcher().string("-!"));
    public static final SymbolType CloseParenthesis = new SymbolType("CloseParenthesis")
            .wikiMatcher(new Matcher().string(")"));
    public static final SymbolType ClosePlainTextTable = new SymbolType("ClosePlainTextTable")
            .wikiMatcher(new Matcher().startLine().string("]!"));
    public static final SymbolType ClosePreformat = new SymbolType("ClosePreformat")
            .wikiMatcher(new Matcher().string("}}}"));
    public static final SymbolType Colon = new SymbolType("Colon")
            .wikiMatcher(new Matcher().string(":"));
    public static final SymbolType Comma = new SymbolType("Comma")
            .wikiMatcher(new Matcher().string(","));
    public static final SymbolType EMail = new SymbolType("EMail")
            .htmlTranslation(new HtmlBuilder("a").bodyContent().attribute("href", -1, "mailto:").inline());
    public static final SymbolType Empty = new SymbolType("Empty");
    public static final SymbolType EndCell = new SymbolType("EndCell")
            .wikiMatcher(new Matcher().string(new String[] {"|\n|", "|\n", "|"}));
    public static final SymbolType Italic = new SymbolType("Italic")
            .wikiMatcher(new Matcher().string("''"))
            .wikiRule(new EqualPairRule())
            .htmlTranslation(new HtmlBuilder("i").body(0).inline());
    public static final SymbolType Meta = new SymbolType("Meta")
            .wikiMatcher(new Matcher().string("!meta"))
            .wikiRule(new LineRule())
            .htmlTranslation(new HtmlBuilder("span").body(0).attribute("class", "meta").inline());
    public static final SymbolType Newline = new SymbolType("Newline")
            .wikiMatcher(new Matcher().string("\n"))
            .htmlTranslation(new HtmlBuilder("br").inline());
    public static final SymbolType NoteLine = new SymbolType("NoteLine")
            .wikiMatcher(new Matcher().string("!note"))
            .wikiRule(new LineRule())
            .htmlTranslation(new HtmlBuilder("span").body(0).attribute("class", "note").inline());
    public static final SymbolType OpenBrace = new SymbolType("OpenBrace")
            .wikiMatcher(new Matcher().string("{"));
    public static final SymbolType OpenBracket = new SymbolType("OpenBracket")
            .wikiMatcher(new Matcher().string("["));
    public static final SymbolType OpenParenthesis = new SymbolType("OpenParenthesis")
            .wikiMatcher(new Matcher().string("("));
    public static final SymbolType OrderedList = new SymbolType("OrderedList")
            .wikiMatcher(new Matcher().startLine().whitespace().string(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"}).string(" "))
            .wikiRule(new ListRule())
            .htmlTranslation(new ListBuilder("ol"));
    public static final SymbolType PlainTextCellSeparator = new SymbolType("PlainTextCellSeparator");
    public static final SymbolType Preformat = new SymbolType("Preformat")
            .wikiMatcher(new Matcher().string("{{{"))
            .wikiRule(new Literal())
            .htmlTranslation(new HtmlBuilder("pre").bodyContent());
    public static final SymbolType Strike = new SymbolType("Strike")
            .wikiMatcher(new Matcher().string("--"))
            .wikiRule(new EqualPairRule())
            .htmlTranslation(new HtmlBuilder("span").body(0).attribute("class", "strike").inline());
    public static final SymbolType Style = new SymbolType("Style")
            .wikiMatcher(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}))
            .wikiRule(new StyleRule())
            .htmlTranslation(new HtmlBuilder("span").body(0).attribute("class", -1).inline());
    public static final SymbolType SymbolList = new SymbolType("SymbolList");
    public static final SymbolType Text = new SymbolType("Text")
            .htmlTranslation(new TextBuilder());
    public static final SymbolType UnorderedList = new SymbolType("UnorderedList")
            .wikiMatcher(new Matcher().startLine().whitespace().string("* "))
            .wikiRule(new ListRule())
            .htmlTranslation(new ListBuilder("ul"));
    public static final SymbolType Whitespace = new SymbolType("Whitespace")
            .wikiMatcher(new Matcher().whitespace());
    public static final SymbolType WikiWord = new SymbolType("WikiWord")
            .htmlTranslation(new WikiWordBuilder());
    
    private String name;
    private Matcher wikiMatcher =  new NullMatcher();
    private Rule wikiRule = null;
    private Translation htmlTranslation = null;

    public SymbolType(String name) { this.name = name; }

    public Matcher getWikiMatcher() { return wikiMatcher; }
    public Rule getWikiRule() { return wikiRule; }
    public Translation getHtmlTranslation() { return htmlTranslation; }

    public SymbolType wikiMatcher(Matcher value) {
        wikiMatcher = value;
        return this;
    }

    public SymbolType wikiRule(Rule value) {
        wikiRule = value;
        return this;
    }

    public SymbolType htmlTranslation(Translation value) {
        htmlTranslation = value;
        return this;
    }

    @Override public String toString() { return name; }
}

