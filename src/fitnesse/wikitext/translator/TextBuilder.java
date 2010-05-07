package fitnesse.wikitext.translator;

import fitnesse.html.HtmlText;
import fitnesse.wikitext.Utils;
import fitnesse.wikitext.parser.Symbol;

public class TextBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        return new HtmlText(Utils.unescapeWiki(symbol.getContent())).html();
    }
}
