package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;

public class AliasBuilder implements Translation {

    public String toHtml(Translator translator, Symbol symbol) {
        if (symbol.childAt(0).childAt(0).getType() == SymbolType.WikiWord) return translator.translate(symbol.childAt(0));
        
        String linkBody = translator.translate(symbol.childAt(0));
        if (symbol.childAt(1).getType() == SymbolType.WikiWord)
            return new WikiWordBuilder().buildLink(translator.getPage(), symbol.childAt(1).getContent(), linkBody);

        HtmlTag alias = new HtmlTag("a", linkBody);
        alias.addAttribute("href", symbol.childAt(1).getContent());
        return alias.htmlInline();
    }
}
