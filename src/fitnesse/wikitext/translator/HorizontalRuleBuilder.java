package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class HorizontalRuleBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag html = new HtmlTag("hr");
        int size = symbol.getContent().length() - 3;
        if (size > 1) html.addAttribute("size", Integer.toString(size));
        return html.html();
    }
}
