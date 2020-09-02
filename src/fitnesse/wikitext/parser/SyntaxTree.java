package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SyntaxTree {
  public static final SyntaxTree EMPTY_TREE = new SyntaxTree(Symbol.emptySymbol);

  public SyntaxTree(Symbol tree) {
    this.tree = tree;
  }

  public String translate(Translator translator) {
    return translator.translateTree(tree);
  }

  public List<Symbol> findHeaderLines() {
    final List<Symbol> symbols = new LinkedList<>();
    for (final Symbol symbol : tree.getChildren()) {
      if (symbol.isType(HeaderLine.symbolType)) {
        symbols.add(symbol);
      }
    }
    return Collections.unmodifiableList(symbols);
  }

  public List<String> findPaths(Translator translator) {
    List<String> result = new ArrayList<>();
    tree.walkPostOrder(new SymbolTreeWalker() {

      @Override
      public boolean visit(Symbol node) {
        if (node.getType() instanceof PathsProvider) {
          result.addAll(((PathsProvider) node.getType()).providePaths(translator, node));
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) {
        return true;
      }
    });
    return result;
  }

  public List<String> findXrefs() {
    List<String> xrefPages = new ArrayList<>();
    tree.walkPreOrder(new SymbolTreeWalker() {
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

  private final Symbol tree;
}
