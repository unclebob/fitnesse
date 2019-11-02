package fitnesse.wiki;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;

/**
 * Utilities for Handling a tree of {@link fitnesse.wikitext.parser.Symbol} aka syntax tree.
 */
public class SymbolUtil {

  /**
   * Walks through the syntax tree either in pre- oder postorder, looking for all symbols of the
   * given type and collects it in a list.
   *
   * @param syntaxTree The syntax tree is a tree of {@link Symbol}.
   * @param type       The {@link SymbolType} of the searched {@link Symbol}. If type is null,
   *                   each type is accepted.
   * @param preorder   Walks through the syntax tree in preorder when <code>true</code>, in
   *                   postorder when <code>fals</code>.
   * @return A List of Symbols.
   */
  public static List<Symbol> findSymbolsByType(final Symbol syntaxTree, final SymbolType type,
                                               final boolean preorder) {
    final List<Symbol> symbols = new LinkedList<>();
    if (preorder) {
      addToSymbolsIfIsOfType(symbols, syntaxTree, type);
    }
    for (Symbol subTree : syntaxTree.getChildren()) {
      symbols.addAll(findSymbolsByType(subTree, type, preorder));
    }
    if (!preorder) {
      addToSymbolsIfIsOfType(symbols, syntaxTree, type);
    }
    return Collections.unmodifiableList(symbols);
  }

  private static void addToSymbolsIfIsOfType(final List<Symbol> symbols, final Symbol symbol,
                                             final SymbolType type) {
    if (type == null) {
      symbols.add(symbol);
    } else {
      if (symbol.isType(type)) {
        symbols.add(symbol);
      }
    }
  }

}
