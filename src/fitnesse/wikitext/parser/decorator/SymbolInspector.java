package fitnesse.wikitext.parser.decorator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class SymbolInspector {

  private final Symbol symbol;

  private SymbolInspector(Symbol symbol) {
    if (isNull(symbol)) {
      throw new NullPointerException("Symbol is null");
    }
    this.symbol = symbol;
  }

  public static SymbolInspector inspect(Symbol symbol) {
    return new SymbolInspector(symbol);
  }

  public void checkSymbolType(SymbolType symbolType) {
    if (!symbolType.matchesFor(symbol.getType())) {
      throw new IllegalStateException(format("Expected symbol of type '%s', but was '%s'", symbolType, symbol.getType()));
    }
  }

  public String getRawContent() {
    final StringBuilder buffer = new StringBuilder();
    symbol.walkPreOrder(node -> {
      buffer.append(node.getContent());
    });

    return buffer.toString().trim();
  }
}
