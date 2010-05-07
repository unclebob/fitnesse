package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;

public interface Translation {
    String toTarget(Translator translator, Symbol symbol);
}
