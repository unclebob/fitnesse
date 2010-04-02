package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public interface Translation {
    String toHtml(Translator translator, Symbol symbol);
}
