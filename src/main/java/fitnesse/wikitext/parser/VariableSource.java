package fitnesse.wikitext.parser;

import util.Maybe;

public interface VariableSource {
    Maybe<String> findVariable(String name);
}
