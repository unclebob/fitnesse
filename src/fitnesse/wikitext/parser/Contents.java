package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;

public class Contents extends SymbolType implements Rule, Translation {
    public static final String FILTER_TOC = "FILTER_TOC";
    public static final String HELP_TOC = "HELP_TOC";
    public static final String MORE_SUFFIX_DEFAULT = " ...";
    public static final String MORE_SUFFIX_TOC = "MORE_SUFFIX_TOC";
    public static final String PROPERTY_TOC = "PROPERTY_TOC";
    public static final String PROPERTY_CHARACTERS = "PROPERTY_CHARACTERS";
    public static final String PROPERTY_CHARACTERS_DEFAULT = "*+@>-";
    public static final String REGRACE_TOC = "REGRACE_TOC";
    public static final String SHOW_TITLES_IN_TOC = "SHOW_TITLES_IN_TOC";

    public Contents() {
        super("Contents");
        wikiMatcher(new Matcher().startLineOrCell().string("!contents"));
        wikiRule(this);
        htmlTranslation(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseToEnd(SymbolType.Newline);
        for (Symbol option: body.getChildren()) {
            if (option.isType(SymbolType.Whitespace)) continue;
            if (!option.getContent().startsWith("-")) return Symbol.nothing;
            current.add(option);
        }

        current.evaluateVariables(
                new String[] {HELP_TOC, REGRACE_TOC, PROPERTY_TOC, FILTER_TOC, MORE_SUFFIX_TOC, PROPERTY_CHARACTERS},
                parser.getVariableSource());

        return new Maybe<Symbol>(current);
    }
    public String toTarget(Translator translator, Symbol symbol) {
        ContentsItemBuilder itemBuilder
                = new ContentsItemBuilder(symbol, 1, translator.getPage());
        HtmlTag contentsDiv = HtmlUtil.makeDivTag("contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        contentsDiv.add(itemBuilder.buildLevel(translator.getPage()));
        return contentsDiv.html();
    }
}
