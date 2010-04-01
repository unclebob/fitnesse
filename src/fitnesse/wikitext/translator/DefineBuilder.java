package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Symbol;

public class DefineBuilder implements Translation {
    public HtmlTag toHtml(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag("span", "variable defined: "
                + translator.translate(symbol.childAt(0))
                + "="
                + translator.translate(symbol.childAt(2)));
        result.addAttribute("class", "meta");
        return result;
    }
}
