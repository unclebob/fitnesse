package fitnesse.wikitext.parser;

import fitnesse.wikitext.VariableSource;

import java.util.Optional;

public class TestVariableSource implements VariableSource {
    private String name;
    private String value;

    public TestVariableSource(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Optional<String> findVariable(String requestedName) {
        return requestedName.equals(name) ? Optional.ofNullable(value) : Optional.empty();
    }
}
