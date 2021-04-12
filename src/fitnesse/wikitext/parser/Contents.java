package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.shared.ContentsItemBuilder;
import fitnesse.wikitext.shared.Names;

public class Contents extends SymbolType implements Rule, Translation {

    public Contents() {
        super("Contents");
        wikiMatcher(new Matcher().startLineOrCell().string("!contents"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseToEnd(SymbolType.Newline);
        for (Symbol child: body.getChildren()) {
            if (child.isType(SymbolType.Whitespace)) continue;
            String option = child.getContent();
            if (!option.startsWith("-")) return Symbol.nothing;
            if (option.equals("-R")) {
              current.putProperty(option, String.valueOf(Integer.MAX_VALUE));
            }
            else if (option.startsWith("-R")) {
              current.putProperty("-R", option.substring(2));
            }
            else {
              current.putProperty(option, "");
            }
        }

        current.copyVariables(new String[] {
                  Names.HELP_TOC,
                  Names.HELP_INSTEAD_OF_TITLE_TOC,
                  Names.REGRACE_TOC,
                  Names.PROPERTY_TOC,
                  Names.FILTER_TOC,
                  Names.MORE_SUFFIX_TOC,
                  Names.PROPERTY_CHARACTERS,
                  Names.TEST_PAGE_COUNT_TOC},
                parser.getVariableSource());

        return new Maybe<>(current);
    }
    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        ContentsItemBuilder itemBuilder
                = new ContentsItemBuilder(symbol, 1, translator.getPage());
        HtmlTag contentsDiv = new HtmlTag("div");
        contentsDiv.addAttribute("class", "contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        contentsDiv.add(itemBuilder.buildLevel(translator.getPage()));
        return contentsDiv.html();
    }
}
