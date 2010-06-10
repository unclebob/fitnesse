package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class Variable extends SymbolType implements Rule, Translation {
    public static final Variable symbolType = new Variable();
    
    public Variable() {
        super("Variable");
        wikiMatcher(new Matcher().string("${"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> tokens = parser.moveNext(new SymbolType[] {SymbolType.Text, SymbolType.CloseBrace});
        if (tokens.size() == 0) return Symbol.nothing;

        String name = tokens.get(0).getContent();
        if (!ScanString.isVariableName(name)) return Symbol.nothing;

        current.add(name);

        Maybe<String> variableValue = parser.getVariableSource().findVariable(name);
        if (variableValue.isNothing()) {
            current.add(new Symbol(SymbolType.Meta).add("undefined variable: " + name));
        }
        else {
            Symbol variableValueSymbol = parser.parse(variableValue.getValue());
            current.add(variableValueSymbol);
        }
        
        return new Maybe<Symbol>(current);
    }

    public String toTarget(Translator translator, Symbol symbol) {
        return translator.translate(symbol.childAt(1));
    }
}
