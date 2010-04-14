package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public interface VariableSource {
    Maybe<String> findVariable(String name, Symbol currentSymbol);
}
