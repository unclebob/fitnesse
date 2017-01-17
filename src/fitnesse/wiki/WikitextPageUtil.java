package fitnesse.wiki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fitnesse.wikitext.parser.Alias;
import fitnesse.wikitext.parser.See;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;

public class WikitextPageUtil {

  public static List<String> getXrefPages(WikitextPage page) {
    final List<String> xrefPages = new ArrayList<>();
    page.getSyntaxTree().walkPreOrder(new SymbolTreeWalker() {
      @Override
      public boolean visit(Symbol node) {
        if (node.isType(See.symbolType)) {
          if (node.childAt(0).isType(Alias.symbolType)) {
            xrefPages.add(node.childAt(0).lastChild().childAt(0).getContent());
          } else {
            xrefPages.add(node.childAt(0).getContent());
          }
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) {
        return true;
      }
    });
    return xrefPages;
  }

  public static List<Symbol> getSymbols(final WikitextPage page, final SymbolType symbolType) {
    final List<Symbol> symbols = new LinkedList<>();
    for (final Symbol symbol : page.getSyntaxTree().getChildren()) {
      if (symbol.isType(symbolType)) {
        symbols.add(symbol);
      }
    }
    return Collections.unmodifiableList(symbols);
  }

}
