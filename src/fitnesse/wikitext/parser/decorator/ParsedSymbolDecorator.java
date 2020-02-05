package fitnesse.wikitext.parser.decorator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.VariableSource;

public interface ParsedSymbolDecorator {

  void handleParsedSymbol(Symbol symbol, VariableSource variableSource);
}
