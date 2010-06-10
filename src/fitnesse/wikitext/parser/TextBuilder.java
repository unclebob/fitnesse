package fitnesse.wikitext.parser;

import fitnesse.html.HtmlText;
import fitnesse.wikitext.Utils;

public class TextBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        return new HtmlText(Utils.unescapeWiki(symbol.getContent())).html();
    }
}
