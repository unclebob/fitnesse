package fitnesse.wikitext.parser;

import util.Maybe;

import fitnesse.html.HtmlTag;

public class HorizontalRule extends SymbolType implements Rule, Translation {
    public HorizontalRule() {
        super("HorizontalRule");
        wikiMatcher(new Matcher().string("---").repeat('-'));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        parser.endBlock();
        return new Maybe<Symbol>(current);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag html = new HtmlTag("hr");
        int size = symbol.getContent().length() - 3;
        if (size > 1) html.addAttribute("size", Integer.toString(size));
        return html.html();
    }
}
