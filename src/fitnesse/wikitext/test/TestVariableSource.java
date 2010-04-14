package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.translator.VariableSource;
import util.Maybe;

public class TestVariableSource implements VariableSource {
    private String name;
    private String value;

    public TestVariableSource(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Maybe<String> findVariable(String requestedName, Symbol currentSymbol) {
        return requestedName.equals(name) ? new Maybe<String>(value) : Maybe.noString;
    }
}
