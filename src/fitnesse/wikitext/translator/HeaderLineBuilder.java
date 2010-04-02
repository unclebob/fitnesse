package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class HeaderLineBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag("h" + symbol.getContent().substring(1));
        result.add(translator.translate(symbol.childAt(0)).trim());
        return result.html();
    }
}
