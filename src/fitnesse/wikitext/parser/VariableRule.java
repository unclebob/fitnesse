package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public class VariableRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Symbol current = parser.getCurrent();
        List<Symbol> tokens = parser.getScanner().nextTokens(new SymbolType[] {SymbolType.Text, SymbolType.CloseBrace});
        if (tokens.size() == 0) return Symbol.Nothing;

        String name = tokens.get(0).getContent();
        if (!ScanString.isVariableName(name)) return Symbol.Nothing;

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
}
