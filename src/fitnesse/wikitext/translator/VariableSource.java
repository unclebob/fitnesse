package fitnesse.wikitext.translator;

import util.Maybe;

public interface VariableSource {
    Maybe<String> findVariable(String name);
}
