package fitnesse.wikitext.translator;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Symbol;

public class StyleBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String body = translator.translate(symbol.childAt(0));
        String style = symbol.getContent().substring(7, symbol.getContent().length() - 1);
        return HtmlUtil.makeSpanTag(style, body).html();
    }
}
