package fitnesse.wikitext.parser;

public class Variable extends SymbolType implements Rule, Translation {
    public static final Variable symbolType = new Variable();
    
    public Variable() {
        super("Variable");
        wikiMatcher(new Matcher().string("${"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Maybe<String> name = parser.parseToAsString(SymbolType.CloseBrace);
        if (name.isNothing() || name.getValue().length() == 0) return Symbol.nothing;
        String variableName = name.getValue();
        if (!ScanString.isVariableName(variableName)) return Symbol.nothing;

        current.add(variableName);

        Maybe<String> variableValue = parser.getVariableSource().findVariable(variableName);
        if (variableValue.isNothing()) {
            current.add(new Symbol(SymbolType.Meta).add("undefined variable: " + variableName));
        }
        else {
            Symbol variableValueSymbol = parser.parseWithParent(variableValue.getValue(), null);
            current.add(variableValueSymbol);
        }
        
        return new Maybe<Symbol>(current);
    }

    public String toTarget(Translator translator, Symbol symbol) {
        return translator.translate(symbol.childAt(1));
    }
}
