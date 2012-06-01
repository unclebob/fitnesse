package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

public class Contents extends SymbolType implements Rule, Translation {
    public static final String FILTER_TOC = "FILTER_TOC";
    public static final String HELP_TOC = "HELP_TOC";
    public static final String MORE_SUFFIX_DEFAULT = " ...";
    public static final String MORE_SUFFIX_TOC = "MORE_SUFFIX_TOC";
    public static final String PROPERTY_TOC = "PROPERTY_TOC";
    public static final String REGRACE_TOC = "REGRACE_TOC";

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
                new String[] {HELP_TOC, REGRACE_TOC, PROPERTY_TOC, FILTER_TOC, MORE_SUFFIX_TOC},
                parser.getVariableSource());

        return new Maybe<Symbol>(current);
    }
    public String toTarget(Translator translator, Symbol symbol) {
        ContentsItemBuilder itemBuilder
                = new ContentsItemBuilder(symbol, 1);
        HtmlTag contentsDiv = HtmlUtil.makeDivTag("contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        HtmlTag div = itemBuilder.buildLevel(translator.getPage(), contentsDiv);
        return div.html();
    }
}
