package fitnesse.wikitext.parser;

public interface VariableSource {
    Maybe<String> findVariable(String name);
}
