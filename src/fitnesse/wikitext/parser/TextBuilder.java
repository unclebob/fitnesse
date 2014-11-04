package fitnesse.wikitext.parser;

import fitnesse.wikitext.Utils;

public class TextBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        return Utils.escapeHTML(Utils.unescapeWiki(symbol.getContent()));
    }
}
