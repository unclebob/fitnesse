package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;


public class LastModifiedBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        return translator.formatMessage("Last modified anonymously on xxx");
    }
}
