package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;

public class HorizontalRule extends SymbolType implements Translation {
    public HorizontalRule() {
        super("HorizontalRule");
        wikiMatcher(new Matcher().string("---").repeat('-'));
        htmlTranslation(this);
    }
    
    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag html = new HtmlTag("hr");
        int size = symbol.getContent().length() - 3;
        if (size > 1) html.addAttribute("size", Integer.toString(size));
        return html.html();
    }
}
