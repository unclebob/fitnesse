package fitnesse.wikitext.parser;

public class TestVariableSource implements VariableSource {
    private String name;
    private String value;

    public TestVariableSource(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Maybe<String> findVariable(String requestedName) {
        return requestedName.equals(name) ? new Maybe<>(value) : Maybe.noString;
    }
}
