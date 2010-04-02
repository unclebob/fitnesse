package fitnesse.wikitext.translator;

import fitnesse.html.HtmlText;
import fitnesse.wikitext.parser.Symbol;

public class TextBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        return new HtmlText(symbol.getContent()).html();
    }
}
