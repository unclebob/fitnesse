package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class ListBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag list = new HtmlTag("ul");
        list.add(new HtmlTag("li", translator.translate(symbol.childAt(0))));
        return list.html();
    }
}
