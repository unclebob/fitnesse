package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.LineRule;
import fitnesse.wikitext.parser.Symbol;

public class HeaderLineBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag("h" + symbol.getProperty(LineRule.Level));
        result.add(translator.translate(symbol.childAt(0)).trim());
        return result.html();
    }
}
