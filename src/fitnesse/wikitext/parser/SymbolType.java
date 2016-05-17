package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class SymbolType implements Matchable {
    private static final Rule defaultRule = new Rule() {
        @Override
        public Maybe<Symbol> parse(Symbol current, Parser parser) {
            return new Maybe<>(current);
        }
    };

    public static final SymbolType Bold = new SymbolType("Bold")
            .wikiMatcher(new Matcher().string("'''"))
            .wikiRule(new EqualPairRule())
            .htmlTranslation(new HtmlBuilder("b").body(0).inline());
    public static final SymbolType CenterLine = new SymbolType("CenterLine")
            .wikiMatcher(new Matcher().startLineOrCell().string("!c"))
            .wikiMatcher(new Matcher().startLineOrCell().string("!C"))
            .wikiRule(new LineRule())
            .htmlTranslation(new HtmlBuilder("center").body(0));
    public static final SymbolType CloseBrace = new SymbolType("CloseBrace")
            .wikiMatcher(new Matcher().string("}"));
    public static final SymbolType CloseBracket = new SymbolType("CloseBracket")
            .wikiMatcher(new Matcher().string("]"));
    public static final SymbolType CloseCollapsible = new SymbolType("CloseCollapsible")
            .wikiMatcher(new Matcher().startLine().repeat('*').string("!"));
    public static final SymbolType CloseEvaluator = new SymbolType("CloseEvaluator")
            .wikiMatcher(new Matcher().string("=}"));
    public static final SymbolType CloseLiteral = new SymbolType("CloseLiteral")
            .wikiMatcher(new Matcher().string("-!"));
    public static final SymbolType CloseNesting = new SymbolType("CloseNesting")
            .wikiMatcher(new Matcher().string(")!"));
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
    public static final SymbolType DateFormatOption = new SymbolType("DateFormatOption")
            .wikiMatcher(new Matcher().string("-xml"))
            .wikiMatcher(new Matcher().string("-t"));
    public static final SymbolType Delta = new SymbolType("Delta")
            .wikiMatcher(new Matcher().string("+").digits())
            .wikiMatcher(new Matcher().string("-").digits());
    public static final SymbolType EMail = new SymbolType("EMail")
            .htmlTranslation(new HtmlBuilder("a").bodyContent().attribute("href", -1, "mailto:").inline());
    public static final SymbolType Empty = new SymbolType("Empty");
    public static final SymbolType EndCell = new SymbolType("EndCell")
            .wikiMatcher(new Matcher().string("|").ignoreWhitespace().string("\n|"))
            .wikiMatcher(new Matcher().string("|").ignoreWhitespace().string("\r\n|"))
            .wikiMatcher(new Matcher().string("|").ignoreWhitespace().string("\n"))
            .wikiMatcher(new Matcher().string("|").ignoreWhitespace().string("\r\n"))
            .wikiMatcher(new Matcher().string("|"));
    public static final SymbolType Italic = new SymbolType("Italic")
            .wikiMatcher(new Matcher().string("''"))
            .wikiRule(new EqualPairRule())
            .htmlTranslation(new HtmlBuilder("i").body(0).inline());
    public static final SymbolType Meta = new SymbolType("Meta")
            .wikiMatcher(new Matcher().startLineOrCell().string("!meta"))
            .wikiRule(new LineRule())
            .htmlTranslation(new HtmlBuilder("span").body(0).attribute("class", "meta").inline());
    public static final SymbolType Newline = new SymbolType("Newline")
            .wikiMatcher(new Matcher().string("\n"))
            .wikiMatcher(new Matcher().string("\r\n"))
            .htmlTranslation(new HtmlBuilder("br").inline());
    public static final SymbolType NoteLine = new SymbolType("NoteLine")
            .wikiMatcher(new Matcher().startLineOrCell().string("!note"))
            .wikiRule(new LineRule())
            .htmlTranslation(new HtmlBuilder("p").body(0).attribute("class", "note").inline());
    public static final SymbolType OpenBrace = new SymbolType("OpenBrace", CloseBrace)
            .wikiMatcher(new Matcher().string("{"));

    public static final SymbolType OpenBracket = new SymbolType("OpenBracket", CloseBracket)
            .wikiMatcher(new Matcher().string("["));
    public static final SymbolType OpenParenthesis = new SymbolType("OpenParenthesis", CloseParenthesis)
            .wikiMatcher(new Matcher().string("("));
    public static final SymbolType OrderedList = new SymbolType("OrderedList")
            .wikiMatcher(new Matcher().startLine().whitespace().listDigit().string(" "))
            .wikiRule(new ListRule())
            .htmlTranslation(new ListBuilder("ol"));
    public static final SymbolType Strike = new SymbolType("Strike")
            .wikiMatcher(new Matcher().string("--"))
            .wikiRule(new EqualPairRule())
            .htmlTranslation(new HtmlBuilder("strike").body(0).inline());
    public static final SymbolType Style = new SymbolType("Style")
            .wikiMatcher(new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}))
            .wikiRule(new StyleRule())
            .htmlTranslation(new HtmlBuilder("span").body(0).attribute("class", -1).inline());
    public static final SymbolType SymbolList = new SymbolType("SymbolList");
    public static final SymbolType Text = new SymbolType("Text")
            .htmlTranslation(new TextBuilder());
    public static final SymbolType UnorderedList = new SymbolType("UnorderedList")
            .wikiMatcher(new Matcher().startLine().whitespace().string("*"))
            .wikiRule(new ListRule())
            .htmlTranslation(new ListBuilder("ul"));
    public static final SymbolType Whitespace = new SymbolType("Whitespace")
            .wikiMatcher(new Matcher().whitespace());

    private String name;
    private List<Matcher> wikiMatchers = new ArrayList<>(1);
    private Rule wikiRule = defaultRule;
    private Translation htmlTranslation = null;
    private final SymbolType closeType;

    public SymbolType(String name) { this(name, Empty); }

    public SymbolType(String name, SymbolType closeType) {
      this.name = name;
      this.closeType = closeType;
    }

  public List<Matcher> getWikiMatchers() { return wikiMatchers; }
    public Rule getWikiRule() { return wikiRule; }
    public Translation getHtmlTranslation() { return htmlTranslation; }

    public SymbolType wikiMatcher(Matcher value) {
        wikiMatchers.add(value);
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

    @Override
    public boolean matchesFor(SymbolType symbolType) {
        return this.name.equals(symbolType.name);
    }

    @Override
    public SymbolMatch makeMatch(ScanString input, SymbolStream symbols) {
        for (Matcher matcher: getWikiMatchers()) {
            Maybe<Integer> matchLength = matcher.makeMatch(input, symbols);
            if (!matchLength.isNothing()) return new SymbolMatch(this, input, matchLength.getValue());
        }
        return SymbolMatch.noMatch;
    }

    public SymbolType closeType() {
      return closeType;
    }
}

